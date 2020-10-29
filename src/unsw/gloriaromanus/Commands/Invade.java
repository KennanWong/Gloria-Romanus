package unsw.gloriaromanus.Commands;

import java.util.List;
import java.util.Random;

import unsw.gloriaromanus.*;

public class Invade extends Command{
    private Faction enemyFaction;
    private Province humanProvince;
    private Province enemyProvince;

    public Invade (String command, Faction humanFaction, Faction enemyFaction, Province humanProvince, Province enemyProvince) {
        super(command, humanFaction);
        this.humanProvince = humanProvince;
        this.enemyProvince = enemyProvince;
        this.enemyFaction = enemyFaction;
    }
    public Faction getEnemyFaction() {
        return enemyFaction;
    }
    public Province getHumanProvince() {
        return humanProvince;
    }
    public Province getEnemyProvince() {
        return enemyProvince;
    }

    // excecute the invade command, will return 1 if we have one, -1 if we lost and 0 if draw
    public int execute()   {
        Faction humanFaction = super.getFaction();
        List <Unit> humanUnits = humanProvince.getUnits();
        List <Unit> enemyUnits = enemyProvince.getUnits();
    
        // army strength calculated as the sum of number of soldiers in unit x attack x defense for all units in the army.

        double humanStrength = 0;
        for (Unit unit: humanUnits) {
            humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (humanStrength == 0) {
            return -1;
        }

        double enemyStrength = 0;
        for (Unit unit: enemyUnits) {
            enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (enemyStrength == 0) {
            // invade by default
            enemyProvince.changeProvinceOwnership(humanFaction);
            humanProvince.moveUnits(enemyProvince);
            return 1;
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
                return 1;
            } 
            if (humanStrength == 0) {
                // enemy won
                humanProvince.changeProvinceOwnership(enemyProvince.getFaction());
                enemyProvince.moveUnits(humanProvince);
                return -1;
                
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
                return 1;
            } 
            if (humanStrength == 0) {
                // enemy won
                humanProvince.changeProvinceOwnership(enemyProvince.getFaction());
                enemyProvince.moveUnits(humanProvince); 
                return -1;
            }

            numEncounters += 1; 
            // recalculate strengths
            
            if (numEncounters >= 200) {
                return 0;
            }
        } 
        return 0;
    }
}
