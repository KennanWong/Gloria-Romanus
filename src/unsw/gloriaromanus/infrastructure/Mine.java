package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Mine extends Building {

    public Mine(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Mine", costMultiplier, buildTimeReduction, province);
    }


}
