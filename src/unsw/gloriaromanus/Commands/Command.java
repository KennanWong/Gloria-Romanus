package unsw.gloriaromanus.Commands;

import unsw.gloriaromanus.*;

public class Command {
    private Strategy strategy;

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public String executeStrategy(Province province1, Province province2) {
        return strategy.execute(province1, province2);
    }
}
