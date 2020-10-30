package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

public class Market extends Building {

    public Market(double costMultiplier, int buildTimeReduction) throws IOException {
        super("Market", costMultiplier, buildTimeReduction);
    }

}