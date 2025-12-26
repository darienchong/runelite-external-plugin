package external;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(
	name = "Auto-Eat"
)
public class AutoEatPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AutoEatConfig config;

	private Set<Integer> healingItemIds;

    private boolean isWaitingToEat;
	private int tickEatDelay = -1;

	private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);
	private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 25, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.DiscardPolicy());

	@Override
	protected void startUp() throws Exception
	{
		// TODO: Add all foods
		healingItemIds = Set.of(
				ItemID.TROUT,
				ItemID.SALMON,
				ItemID.TUNA,
				ItemID.SWORDFISH,
				ItemID.LOBSTER
		);
	}

	@Override
	protected void shutDown() throws Exception
	{
		healingItemIds = null;
	}

	// Under threshold and missing health
	private boolean isUnderHitpointThreshold() {
		return client.getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpThreshold() &&
				client.getRealSkillLevel(Skill.HITPOINTS) - client.getBoostedSkillLevel(Skill.HITPOINTS) > 0;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		autoEat();
	}

	@Provides
	AutoEatConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AutoEatConfig.class);
	}

	public void autoEat() {
		if (healingItemIds == null) {
			log.debug("Healing item IDs weren't set");
			return;
		}

		if (!isUnderHitpointThreshold()) {
			return;
		}

		if (!isWaitingToEat) {
			isWaitingToEat = true;
            tickEatDelay = helper.Utils.getRandomIntBetweenRange(config.minDelay(), config.maxDelay());
			log.debug("Detected below HP threshold ({}/{} < {}), waiting for {} ticks before eating", client.getBoostedSkillLevel(Skill.HITPOINTS), client.getRealSkillLevel(Skill.HITPOINTS), config.hpThreshold(), tickEatDelay);
			return;
		}

		if (tickEatDelay > 0) {
			tickEatDelay--;
			log.debug("Waiting for {} ticks before eating", tickEatDelay);
			return;
		}

		var inventory = client.getWidget(InterfaceID.Inventory.ITEMS);
		if (inventory == null) {
			log.debug("Couldn't retrieve inventory");
			return;
		}
		if (inventory.isHidden()) {
			log.debug("Inventory is hidden, open the inventory");
			// TODO: Auto-swap to inventory tab
			return;
		}

		var inventoryItems = inventory.getChildren();
		if (inventoryItems == null) {
			log.debug("inventory had no children");
			return;
		}

		var found = false;
		for (var item : inventoryItems) {
			if (!healingItemIds.contains(item.getItemId())) {
				continue;
			}

			found = true;
			log.debug("Preparing to eat (name={}, text={}, hidden={}, self_hidden={})", helper.Utils.getItemName(item), item.getText(), item.isHidden(), item.isSelfHidden());
			if (!config.dryRun()) {
				executorService.submit(() -> {
					log.debug("clicking <x={}, y={}> to auto-eat {}", item.getBounds().getX(), item.getBounds().getY(), helper.Utils.getItemName(item));

					helper.Utils.click(client, item.getBounds());
				});
			}

			isWaitingToEat = false;
			tickEatDelay = -1;
			break;
		}

		if (!found) {
			log.debug("didn't find any healing items in the inventory");
			return;
		}
	}
}
