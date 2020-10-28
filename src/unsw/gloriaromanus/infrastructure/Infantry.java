package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Infantry extends Building implements TroopBuilding {

    public Infantry(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Infantry", costMultiplier, buildTimeReduction);
    }


}
