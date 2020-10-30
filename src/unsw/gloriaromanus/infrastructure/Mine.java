package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Mine extends Building {

    public Mine(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Mine", costMultiplier, buildTimeReduction);
    }


}
