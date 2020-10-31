package unsw.gloriaromanus.infrastructure;
import java.io.IOException;

import unsw.gloriaromanus.*;

public class Market extends Building {
    private double bonus;

    public Market(double bonus, double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        super("Market", costMultiplier, buildTimeReduction, province);
        this.bonus = bonus;
    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

}