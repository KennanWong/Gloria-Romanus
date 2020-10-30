package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Infantry extends Building {

    public Infantry(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Infantry", costMultiplier, buildTimeReduction);
    }


}
