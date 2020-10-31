package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Mine extends Building {
    private double bonus;
    
    public Mine(double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Mine", costMultiplier, buildTimeReduction, province);
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }


}
