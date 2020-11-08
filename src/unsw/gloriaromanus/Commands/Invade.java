package unsw.gloriaromanus.Commands;

import java.util.List;
import java.util.Random;

import unsw.gloriaromanus.*;

public class Invade implements Strategy {

    @Override
    public String execute(Province province1, Province province2) {
        Province humanProvince = province1;
        Faction humanFaction = humanProvince.getFaction();
        Province enemyProvince = province2;


        if (humanProvince.isLocked()) {
            return "Cannot use units from a province invaded in the current turn";
        }
        List <Unit> humanUnits = humanProvince.getUnits();
        List <Unit> enemyUnits = enemyProvince.getUnits();
          
        // army strength calculated as the sum of number of soldiers in unit x attack x defense for all units in the army.
        double humanStrength = 0;
        for (Unit unit: humanUnits) {
            humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (humanStrength == 0) {
            return "Lost battle!";
        }

        double enemyStrength = 0;
        for (Unit unit: enemyUnits) {
            enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (enemyStrength == 0) {
            // invade by default
            enemyProvince.changeProvinceOwnership(humanFaction);
            humanProvince.moveUnits(enemyProvince);
            return "Won battle!";
        }

        boolean finished = false;
        int numEncounters = 0;
        while (!finished) {
            double chanceOfWinning = humanStrength/(humanStrength + enemyStrength);
            Random r = new Random();
            double chance = r.nextDouble();

            double winningStength = 0;
            double losingStrength = 0;
            List <Unit> winningUnits;
            List <Unit> losingUnits;
            if (chance < chanceOfWinning) {
                // human won
                winningStength = humanStrength;
                losingStrength = enemyStrength;
                winningUnits = humanUnits;
                losingUnits = enemyUnits;
            } else {
                // enemy won
                winningStength = enemyStrength;
                losingStrength = humanStrength;
                winningUnits = enemyUnits;
                losingUnits = humanUnits;
            }

            
            double percentageOfLoser = 0.0;

            while (percentageOfLoser == 0.0) {
                double tmp = r.nextDouble();
                if (tmp >= (winningStength)/(winningStength + losingStrength)) {
                percentageOfLoser = tmp;
                }
            }

            for (Unit unit: losingUnits) {
                unit.setNumTroops((int)((double)unit.getNumTroops()*(1-percentageOfLoser)));
            }

            humanStrength = 0;
            for (Unit unit: humanUnits) {
                humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
            }

            enemyStrength = 0;
            for (Unit unit: enemyUnits) {
                enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
            }
            if (enemyStrength == 0) {
                // human won
                enemyProvince.changeProvinceOwnership(humanFaction);
                humanProvince.moveUnits(enemyProvince);
                enemyProvince.lockDownProvince();
                return "Won battle!";
            } 
            if (humanStrength == 0) {
                // enemy won
                return "Lost battle!";
                
            }

            double percentageOfWinner = 0.0;

            while (percentageOfWinner == 0.0) {
                double tmp = r.nextDouble();
                if (tmp <= (losingStrength)/(winningStength + losingStrength)) {
                percentageOfWinner = tmp;
                }
            }

            for (Unit unit: winningUnits) {
                unit.setNumTroops((int)((double) unit.getNumTroops()*(1-percentageOfWinner)));
                
            }

            humanStrength = 0;
            for (Unit unit: humanUnits) {
                humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
            }

            enemyStrength = 0;
            for (Unit unit: enemyUnits) {
                enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
            }
            if (enemyStrength == 0) {
                // human won
                enemyProvince.changeProvinceOwnership(humanFaction);
                humanProvince.moveUnits(enemyProvince);
                enemyProvince.lockDownProvince();
                return "Won battle!";
            } 
            if (humanStrength == 0) {
                // enemy won
                return "Lost battle";
            }

            numEncounters += 1; 
            // recalculate strengths
            
            if (numEncounters >= 200) {
                return "Drew battle!";
            }
        } 
        return "Drew battle!";
    }
    
    
}
