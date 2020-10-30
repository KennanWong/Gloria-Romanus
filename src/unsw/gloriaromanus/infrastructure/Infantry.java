package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Infantry extends Building implements TroopBuilding {

    public Infantry(double costMultiplier, int buildTimeReduction, int turnNumber) throws IOException {
        super("Infantry", costMultiplier, buildTimeReduction, turnNumber);
    }


}
