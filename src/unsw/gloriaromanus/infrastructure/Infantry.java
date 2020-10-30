package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Infantry extends Building {

    public Infantry(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Infantry", costMultiplier, buildTimeReduction, province);
    }


}
