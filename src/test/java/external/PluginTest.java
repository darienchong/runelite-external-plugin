package external;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

import java.util.Arrays;

public class PluginTest
{
	@SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception
	{
		@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
		var externalPlugins = Arrays.asList(
			AutoEatPlugin.class
		);
		for (var pluginClass : externalPlugins) {
			ExternalPluginManager.loadBuiltin(pluginClass);
		}
		RuneLite.main(args);
	}
}