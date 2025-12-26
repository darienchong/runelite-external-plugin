package external;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("Auto-Eat")
public interface AutoEatConfig extends Config
{
	@ConfigItem(
			keyName = "dryrun",
			name = "Dry Run",
			description = "Whether to actually eat food or not",
			position = 0
	)
	default boolean dryRun()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hp_threshold",
		name = "Hitpoint Threshold",
		description = "How many hitpoints before eating food",
		position = 1
	)
	@Range(min = 1)
	default int hpThreshold()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "minDelay",
		name = "Min Delay",
		description = "Minimum tick delay before trying to eat",
		position = 2
	)
	@Range(min = 3)
	default int minDelay()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "maxDelay",
		name = "Max Delay",
		description = "Maximum tick delay before trying to eat",
		position = 3
	)
	@Range(min = 6)
	default int maxDelay()
	{
		return 6;
	}
}
