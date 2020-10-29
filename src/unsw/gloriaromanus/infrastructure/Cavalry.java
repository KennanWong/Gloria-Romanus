package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Cavalry extends Building implements TroopBuilding {

    public Cavalry(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Cavalry", costMultiplier, buildTimeReduction);
    }

    
}
