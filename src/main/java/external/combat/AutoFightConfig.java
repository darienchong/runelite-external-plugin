package external.combat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("Auto-Fight")
public interface AutoFightConfig extends Config
{
	@ConfigItem(
		keyName = "inCombatDetectionDelay",
		name = "In-Combat Detection Delay",
		description = "How many game ticks to wait before considering the player as out of combat",
		position = 0
	)
	@Range(min = 3)
	default int inCombatDetectionDelay()
	{
		return 3;
	}

	@ConfigItem(
			keyName = "pathingDelay",
			name = "Pathing Delay",
			description = "How many game ticks to wait while pathing to a mob",
			position = 1
	)
	@Range(min = 10)
	default int pathingDelay()
	{
		return 10;
	}

	@ConfigItem(
			keyName = "clickJitter",
			name = "Click Jitter",
			description = "The amount of noise to apply when determining where on the NPC to click to attack",
			position = 2
	)
	@Range(min = 10)
	default int clickJitter()
	{
		return 10;
	}


}
