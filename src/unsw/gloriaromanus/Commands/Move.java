package unsw.gloriaromanus.Commands;

import unsw.gloriaromanus.*;

public class Move extends Command{
    private Province moveFrom;
    private Province moveTo;

    public Move(String command, Faction faction, Province moveFrom, Province moveTo) {
        super(command, faction);
        this.moveFrom = moveFrom;
        this.moveTo = moveTo;
    }

    public Province getMoveFrom() {
        return moveFrom;
    }

    public Province getMoveTo() {
        return moveTo;
    }

    public void execute() {
        moveFrom.moveUnits(moveTo);
    }
}
