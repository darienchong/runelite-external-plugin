package external.combat;

import lombok.Data;

import java.awt.*;

@Data
public class AutoFightStateInfo {
    private AutoFightState state = AutoFightState.UNKNOWN;
    private int pathingToMobRemainingTicks;
    private Point lastTarget;

    public void tickPathingToMobRemainingTicks() {
        if (pathingToMobRemainingTicks == 0) { return; }
        pathingToMobRemainingTicks--;
    }

    public String toString() {
        return String.format("(state=%s, pathing_to_mob_remaining_ticks=%d)", state.name(), pathingToMobRemainingTicks);
    }
}
