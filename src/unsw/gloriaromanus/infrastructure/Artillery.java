package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Artillery extends Building implements TroopBuilding {

    public Artillery(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Artillery", costMultiplier, buildTimeReduction);
    }

}
