package unsw.gloriaromanus.Commands;

import unsw.gloriaromanus.Province;

public class Move implements Strategy {

    @Override
    public String execute(Province moveFrom, Province moveTo) {
        moveFrom.moveUnits(moveTo);
        return null;
    }
    
    
}
