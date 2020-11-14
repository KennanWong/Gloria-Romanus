package unsw.gloriaromanus.Commands;

import java.util.List;
import java.util.Random;

import unsw.gloriaromanus.Unit;
import unsw.gloriaromanus.Province;

public class Move implements Strategy {
    double unitDecay = 0;
    public Move(int movementPointsReq, int currentMovementPoints) {
        unitDecay = 0.2;
        unitDecay -= (currentMovementPoints - movementPointsReq) * 0.05;
        if (unitDecay < 0.0) {
            unitDecay = 0.0;
        }

    }

    @Override
    public String execute(Province moveFrom, Province moveTo) {
        List<Unit> unitsToMove = moveFrom.getSelectedUnits();
        if (unitsToMove.size() == 0) {
            return "Please select units to move";
        }
        Random r = new Random();
        for (Unit unit : unitsToMove) {
            int numLost = (int) Math.ceil((double) unit.getNumTroops() * unitDecay);
            if (numLost == 0) {
                numLost = 1;
            }
            int casualties = r.nextInt(numLost + 1);
            unit.setNumTroops(unit.getNumTroops() - casualties);
        }
        moveTo.addUnits(unitsToMove);
        // moveFrom.moveUnits(moveTo);
        return "Sucessfully moved units";
    }
    
    
}
