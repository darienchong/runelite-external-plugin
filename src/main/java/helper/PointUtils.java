package helper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;

@Slf4j
public class PointUtils {
    public static Point getNPCCanvasPoint(Client client, NPC npc, int jitter) {
        assert client.isClientThread();

        var randX = Utils.getRandomIntBetweenRange(-jitter, jitter);
        var randY = Utils.getRandomIntBetweenRange(-jitter, jitter);
        var jitteredPoint = new LocalPoint(npc.getLocalLocation().getX() + randX, npc.getLocalLocation().getY() + randY, npc.getLocalLocation().getWorldView());

        final var truePos =  Perspective.localToCanvas(client, npc.getLocalLocation(), npc.getWorldArea().getPlane(), npc.getLogicalHeight() >> 1);
        final var jitteredPos = Perspective.localToCanvas(client, jitteredPoint, npc.getWorldArea().getPlane(), npc.getLogicalHeight() >> 1);
        if (truePos == null || jitteredPos == null) {
            return null;
        }

        log.debug("Comparing jittered point ({}, {}) to true position ({}, {})", jitteredPos.getX(), jitteredPos.getY(), truePos.getX(), truePos.getY());
        return jitteredPos;
    }
}
