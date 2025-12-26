package external;

import external.combat.AutoEatPlugin;
import external.combat.AutoFightPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

import java.util.Arrays;

public class PluginTest
{
	@SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(
			AutoFightPlugin.class,
			AutoEatPlugin.class
		);
		RuneLite.main(args);
	}
}