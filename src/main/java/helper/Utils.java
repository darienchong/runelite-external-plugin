package helper;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

public class Utils {
    /**
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link net.runelite.client.callback.ClientThread}
     * it will result in a crash/desynced thread.
     */
    public static void click(Client client, Rectangle rectangle)
    {
        assert !client.isClientThread();
        Point point = getClickPoint(rectangle);
        click(client, point);
    }

    public static void click(Client client, Point p)
    {
        assert !client.isClientThread();

        if (client.isStretchedEnabled())
        {
            final Dimension stretched = client.getStretchedDimensions();
            final Dimension real = client.getRealDimensions();
            final double width = (stretched.width / real.getWidth());
            final double height = (stretched.height / real.getHeight());
            final Point point = new Point((int) (p.getX() * width), (int) (p.getY() * height));
            mouseEvent(501, client, point);
            mouseEvent(502, client, point);
            mouseEvent(500, client, point);
            return;
        }
        mouseEvent(501, client, p);
        mouseEvent(502, client, p);
        mouseEvent(500, client, p);
    }

    public static Point getClickPoint(Rectangle rect)
    {
        final int x = (int) (rect.getX() + getRandomIntBetweenRange((int) rect.getWidth() / 6 * -1, (int) rect.getWidth() / 6) + rect.getWidth() / 2);
        final int y = (int) (rect.getY() + getRandomIntBetweenRange((int) rect.getHeight() / 6 * -1, (int) rect.getHeight() / 6) + rect.getHeight() / 2);

        return new Point(x, y);
    }

    public static int getRandomIntBetweenRange(int min, int max)
    {
        return (int) ((Math.random() * ((max - min) + 1)) + min);
    }

    private static void mouseEvent(int id, Client client, Point point)
    {
        MouseEvent e = new MouseEvent(
                client.getCanvas(), id,
                System.currentTimeMillis(),
                0, (int) point.getX(), (int) point.getY(),
                1, false, 1
        );

        client.getCanvas().dispatchEvent(e);
    }

    // Matches an item name surrounded by <col> tags
    final static String ITEM_NAME_REGEX = "(?:<col=[a-zA-Z0-9]+>)*([a-zA-Z0-9]+)(?:</col>)*";
    final static Pattern ITEM_NAME_PATTERN = Pattern.compile(ITEM_NAME_REGEX);

    public static String getItemName(Widget item) {
        var matcher = ITEM_NAME_PATTERN.matcher(item.getName());
        if (!matcher.matches()) {
            return item.getName();
        }

        return matcher.group(0);
    }
}
