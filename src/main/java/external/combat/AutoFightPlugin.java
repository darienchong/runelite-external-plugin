package external.combat;

import com.google.inject.Provides;
import helper.InCombatTracker;
import helper.PointUtils;
import helper.Utils;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
    name = "Auto-Fight",
    tags = "external"
)
public class AutoFightPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoFightOverlay autoFightOverlay;

    @Inject
    private Client client;

    @Inject
    private AutoFightConfig config;

    private InCombatTracker combatTracker;
    public AutoFightStateInfo stateInfo;

    @Override
    protected void startUp()
    {
        combatTracker = new InCombatTracker();
        stateInfo = new AutoFightStateInfo();
        overlayManager.add(autoFightOverlay);
    }

    @Override
    protected void shutDown()
    {
        combatTracker = null;
        stateInfo = null;
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (stateInfo == null) {
            log.debug("state info is not yet initialised");
            return;
        }

        stateInfo.tickPathingToMobRemainingTicks();

        log.debug("[{}] state info: {}", client.getGameState(), stateInfo.toString());

        switch (stateInfo.getState()) {
            case UNKNOWN:
                initStateInfo();
                break;
            case IN_COMBAT:
                inCombat();
                break;
            case PATHING_TO_MOB:
                pathingToMob();
                break;
            case LOOKING_FOR_MOB:
                lookForMob();
                break;
        }
    }

    @Provides
    AutoFightConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(AutoFightConfig.class);
    }

    private void initStateInfo() {
        if (stateInfo.getState() != AutoFightState.UNKNOWN) {
            return;
        }

        if (combatTracker.inCombat(client, config.inCombatDetectionDelay())) {
            stateInfo.setState(AutoFightState.IN_COMBAT);
        } else {
            stateInfo.setState(AutoFightState.LOOKING_FOR_MOB);
        }
    }

    private void inCombat() {
        assert client.isClientThread();

        if (stateInfo.getState() != AutoFightState.IN_COMBAT) {
            return;
        }

        if (!combatTracker.inCombat(client, config.inCombatDetectionDelay())) {
            stateInfo.setState(AutoFightState.LOOKING_FOR_MOB);
        }
    }

    private void pathingToMob() {
        assert client.isClientThread();

        if (stateInfo.getState() != AutoFightState.PATHING_TO_MOB) {
            return;
        }

        if (combatTracker.inCombat(client, config.inCombatDetectionDelay())) {
            stateInfo.setState(AutoFightState.IN_COMBAT);
            return;
        }

        if (stateInfo.getPathingToMobRemainingTicks() <= 0) {
            // Not in combat but maximum allocated ticks for pathing has failed
            // Return to looking for mob
            stateInfo.setState(AutoFightState.LOOKING_FOR_MOB);
        }
    }

    private void lookForMob() {
        assert client.isClientThread();

        if (stateInfo.getState() != AutoFightState.LOOKING_FOR_MOB) {
            return;
        }

        // TODO: Change mob names (id?) to use config
        var validMobs = client.getTopLevelWorldView().
                npcs().
                stream().
                filter(npc ->
                    !npc.isDead() && // Not dead
                    !npc.isInteracting() && // Not in combat with other players
                    ArrayUtils.contains(npc.getComposition().getActions(), "Attack") && // Attackable mobs
                    "Zombie".equals(npc.getName())).
                collect(Collectors.toList());
        if (validMobs.isEmpty()) {
            log.debug("didn't find any valid mobs to attack");
            return;
        }

        final var playerPos = client.getLocalPlayer().getLocalLocation();
        validMobs.sort((Comparator<NPC>) (o1, o2) -> {
            var o1Pos = o1.getLocalLocation();
            var o2Pos = o2.getLocalLocation();
            return Utils.squaredDistanceBetween(o1Pos.getX(), o1Pos.getY(), playerPos.getX(), playerPos.getY()) - Utils.squaredDistanceBetween(o2Pos.getX(), o2Pos.getY(), playerPos.getX(), playerPos.getY());
        });

        final NPC chosenMob = validMobs.get(0);
        final var chosenMobCanvasPoint = PointUtils.getNPCCanvasPoint(client, chosenMob, config.clickJitter());
        if (chosenMobCanvasPoint == null) {
            log.debug("failed to get canvas point for chosen mob (id={}, name={}, local_x={}, local_y={})", chosenMob.getId(), chosenMob.getName(), chosenMob.getLocalLocation().getX(), chosenMob.getLocalLocation().getY());
            return;
        }

        var p = new Point(chosenMobCanvasPoint.getX(), chosenMobCanvasPoint.getY());
        SwingUtilities.invokeLater(() -> {
            Utils.click(client, p, true);
        });

        stateInfo.setLastTarget(p);
        stateInfo.setPathingToMobRemainingTicks(config.pathingDelay());
        stateInfo.setState(AutoFightState.PATHING_TO_MOB);
    }
}
