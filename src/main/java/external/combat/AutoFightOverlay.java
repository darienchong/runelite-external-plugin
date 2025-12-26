package external.combat;

import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class AutoFightOverlay extends OverlayPanel
{
	private int maxWidth;
	// private final Client client;
	// private final AutoFightConfig config;
	private final AutoFightPlugin plugin;

	@Inject
	private AutoFightOverlay(/* Client client, AutoFightConfig config, */AutoFightPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		// this.config = config;
		// this.client = client;
		this.plugin = plugin;
		addMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Auto-Fight overlay");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin == null || plugin.stateInfo == null) {
			return null;
		}

		final var currState = plugin.stateInfo.getState();
		String currStateText = "?";
		switch (currState) {
            case UNKNOWN:
				currStateText = "Unknown";
                break;
            case IN_COMBAT:
				currStateText = "In Combat";
                break;
            case LOOKING_FOR_MOB:
				currStateText = "Looking for a target";
                break;
            case PATHING_TO_MOB:
				var x = "?";
				if (plugin.stateInfo.getLastTarget() != null) {
					x = String.format("%d", (int) plugin.stateInfo.getLastTarget().getX());
				}
				var y = "?";
				if (plugin.stateInfo.getLastTarget() != null) {
					y = String.format("%d", (int) plugin.stateInfo.getLastTarget().getY());
				}

				currStateText = String.format("Pathing to (%s, %s) (%d ticks remaining)", x, y, plugin.stateInfo.getPathingToMobRemainingTicks());
                break;
        }
		if (maxWidth < graphics.getFontMetrics().stringWidth(currStateText) + 10) {
			maxWidth = graphics.getFontMetrics().stringWidth(currStateText) + 10;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text(currStateText)
			.build());

		panelComponent.setPreferredSize(new Dimension(maxWidth, 0));

		return super.render(graphics);
	}

}
