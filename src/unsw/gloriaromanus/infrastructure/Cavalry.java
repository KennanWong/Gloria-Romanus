package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Cavalry extends Building {

    public Cavalry(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Cavalry", costMultiplier, buildTimeReduction, province);
    }

    
}
