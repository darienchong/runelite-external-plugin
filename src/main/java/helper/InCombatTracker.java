package helper;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarbitID;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class InCombatTracker {
    private int lastCombatActionTickCount = 0;

    public boolean inCombat(Client client, int tickDelayBeforeOutOfCombat) {
        assert client.isClientThread();

        final Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null)
        {
            log.debug("failed to determine local player");
            return false;
        }
        final Actor interacting = localPlayer.getInteracting();
        if ((interacting instanceof NPC && ArrayUtils.contains(((NPC) interacting).getComposition().getActions(), "Attack")) || (interacting instanceof Player && client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 1)) {
            lastCombatActionTickCount = client.getTickCount();
            return true;
        }

        return lastCombatActionTickCount > 0 && client.getTickCount() - lastCombatActionTickCount < tickDelayBeforeOutOfCombat;
    }
}
