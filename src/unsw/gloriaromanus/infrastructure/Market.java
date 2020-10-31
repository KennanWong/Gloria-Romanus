package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Market extends Building {
    private double bonus;

    public Market(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Market", costMultiplier, buildTimeReduction, province);
    }
}