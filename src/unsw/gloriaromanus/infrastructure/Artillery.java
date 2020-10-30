package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Artillery extends Building {

    public Artillery(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Artillery", costMultiplier, buildTimeReduction, province);
    }

}
