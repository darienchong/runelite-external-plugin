package helper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

@Slf4j
public class Utils {
    /**
     * This method must be called on a new
     * thread, if you try to call it on
     * {@link net.runelite.client.callback.ClientThread}
     * it will result in a crash/desynced thread.
     */
    public static void click(Client client, Rectangle rectangle, boolean moveMouse)
    {
        assert !client.isClientThread();
        Point point = getClickPoint(rectangle);
        click(client, point, moveMouse);
    }

    public static void trySleep(int low, int high) {
        try {
            // Give time for the NPC options to appear
            Thread.sleep(getRandomIntBetweenRange(low, high));
        } catch (InterruptedException ignored) {
        }
    }

    public static void click(Client client, Point p, boolean moveMouse)
    {
        assert !client.isClientThread();

        if (client.isStretchedEnabled())
        {
            final Dimension stretched = client.getStretchedDimensions();
            final Dimension real = client.getRealDimensions();
            final double width = (stretched.width / real.getWidth());
            final double height = (stretched.height / real.getHeight());
            p = new Point((int) (p.getX() * width), (int) (p.getY() * height));
        }

        if (moveMouse) {
            mouseEvent(MouseEvent.MOUSE_MOVED, client, p);
            trySleep(50, 100);
        }
        mouseEvent(MouseEvent.MOUSE_PRESSED, client, p);
        trySleep(40, 200);
        mouseEvent(MouseEvent.MOUSE_RELEASED, client, p);
        mouseEvent(MouseEvent.MOUSE_CLICKED, client, p);
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
        client.getCanvas().dispatchEvent(new MouseEvent(
            client.getCanvas(),
            id,
            System.currentTimeMillis(),
            0,
            (int) point.getX(),
            (int) point.getY(),
            (int) point.getX(),
            (int) point.getY(),
            1,
            false,
            MouseEvent.BUTTON1
        ));
    }

    // Matches an item name surrounded by <col> tags
    final static String ITEM_NAME_REGEX = "(<col=(.*)+>)?([a-zA-Z0-9]+)(</col>)?";
    final static Pattern ITEM_NAME_PATTERN = Pattern.compile(ITEM_NAME_REGEX);

    public static String stripItemNameTags(String rawItemName) {
        try {
            var matcher = ITEM_NAME_PATTERN.matcher(rawItemName);
            if (!matcher.find()) {
                return rawItemName;
            }

            return matcher.group(3);
        } catch (Exception e) {
            return rawItemName;
        }
    }

    public static int squaredDistanceBetween(int x1, int y1, int x2, int y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }
}
