package unsw.gloriaromanus.Commands;

import java.util.ArrayList;
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
        List <Unit> routedEngagingUnits = new ArrayList<>();
        List <Unit> routedOpposingUnits = new ArrayList<>();

          
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
            enemyProvince.lockDownProvince();
            return "Won battle!";
        }

        boolean finished = false;
        int numEncounters = 0;
        while (!finished) {
            // Select a unit at random
            Random r = new Random();
            int humanUnitIndex = r.nextInt(humanUnits.size());
            Unit engagingUnit = humanUnits.get(humanUnitIndex);
            int enemyUnitIndex = r.nextInt(enemyUnits.size());
            Unit opposingUnit = enemyUnits.get(enemyUnitIndex);


            // Begin skirmish
            // Skrimish will end once a unit is destroyed or routed
            boolean skirmishEnd = false;
            while (!skirmishEnd) {
                numEncounters += 1;
                String engagementType;
                if (engagingUnit.getAttackType().equals(opposingUnit.getAttackType())) {
                    engagementType = engagingUnit.getAttackType();
                } else {
                    Unit meleeUnit = null;
                    Unit rangedUnit = null;
                    switch (engagingUnit.getAttackType()) {
                        case "melee":                        
                            meleeUnit = engagingUnit;
                            rangedUnit = opposingUnit;
                            break;
                        case "ranged":
                            rangedUnit = engagingUnit;
                            meleeUnit = opposingUnit;
                            break;
                        
                    }
    
                    double chanceOfMelee = 0.5;
                    chanceOfMelee += 0.1 * (meleeUnit.getSpeed() - rangedUnit.getSpeed());
                    if (chanceOfMelee >= 0.95) {
                        chanceOfMelee = 0.95;
                    }
                    double chance = r.nextDouble();
                    if (chance < chanceOfMelee) {
                        // melee engagement
                        engagementType = "melee";
                    } else {
                        // ranged engagement
                        engagementType = "ranged";
                    }
                }
    
                // 2. Inflict casualties
                int numEngagingTroops = engagingUnit.getNumTroops();
                int numOpposingTroops = opposingUnit.getNumTroops();
                int numCasualtiesOpposing = 0;
                int numCasualtiesEngaging = 0;
                switch (engagementType) {
                    case "melee":
                        numCasualtiesOpposing = meleeEngagement(engagingUnit, opposingUnit);
                        numCasualtiesEngaging = meleeEngagement(opposingUnit, engagingUnit);
                        break;
                    case "ranged":
                        numCasualtiesOpposing = rangedEngagement(engagingUnit, opposingUnit);
                        numCasualtiesEngaging = rangedEngagement(opposingUnit, engagingUnit);
                        break;
                }
                engagingUnit.setNumTroops(numEngagingTroops - numCasualtiesEngaging);
                opposingUnit.setNumTroops(numOpposingTroops - numCasualtiesOpposing);
    
                if (engagingUnit.getNumTroops() <= 0) {
                    // engaging unit lost the battle
                    humanUnits.remove(engagingUnit);
                    skirmishEnd = true;
                    break;
                }
                else if (opposingUnit.getNumTroops() <= 0) {
                    // opposing unit lost the battle
                    enemyUnits.remove(opposingUnit);
                    skirmishEnd = true;
                    break;
                }

                // 3. Breaking of unit chancOfBreakingEngaging = CoBEngaging
                if (!engagingUnit.isBroken()) {
                    double CoBEngaging = 1.0 - (double) engagingUnit.getMorale()*0.1;
                    CoBEngaging += (((double)numCasualtiesEngaging)/((double)numEngagingTroops))/(((double)numCasualtiesOpposing)/((double)numOpposingTroops))*0.1;
                    if (CoBEngaging < 0.05) {
                        CoBEngaging = 0.05;
                    } else if (CoBEngaging > 1) {
                        CoBEngaging = 1;
                    }
                    double CoBOutcomeEngaging = r.nextDouble();
                    if (CoBOutcomeEngaging < CoBEngaging) {
                        engagingUnit.setBroken(true);
                    }
                }

                if (!opposingUnit.isBroken()) {
                    double CoBOpposing = 1.0  - (double) opposingUnit.getMorale()*0.1;
                    CoBOpposing += (((double)numCasualtiesOpposing)/((double)numOpposingTroops))/(((double)numCasualtiesEngaging)/((double)numEngagingTroops)) *0.1;
                    if (CoBOpposing < 0.05) {
                        CoBOpposing= 0.05;
                    } else if (CoBOpposing > 1) {
                        CoBOpposing = 1;
                    }

                    double CoBOutcomeOpposing = r.nextDouble();
                    if (CoBOutcomeOpposing < CoBOpposing) {
                        opposingUnit.setBroken(true);
                    }
                }
                

                // 4. Routing of unit
                if (opposingUnit.isBroken() && engagingUnit.isBroken()) {
                    opposingUnit.setBroken(false);
                    engagingUnit.setBroken(false);
                    skirmishEnd = true;
                    break;
                }

                if (engagingUnit.isBroken()) {
                    double chanceOfRouting = 0.5 + 0.1 * (double) (engagingUnit.getSpeed() - opposingUnit.getSpeed());
                    if (chanceOfRouting < 0.1) {
                        chanceOfRouting = 0.1;
                    } else if (chanceOfRouting > 1) {
                        chanceOfRouting = 1;
                    }
                    if (r.nextDouble() <  chanceOfRouting) {
                        // unit routed successfully
                        routedEngagingUnits.add(engagingUnit);
                        humanUnits.remove(engagingUnit);
                        skirmishEnd = true;
                        break;
                    }
                }
                if (opposingUnit.isBroken()) {
                    double chanceOfRouting = 0.5 + 0.1 * (double) (opposingUnit.getSpeed() - engagingUnit.getSpeed());
                    if (chanceOfRouting < 0.1) {
                        chanceOfRouting = 0.1;
                    } else if (chanceOfRouting > 1) {
                        chanceOfRouting = 1;
                    }
                    if (r.nextDouble() <  chanceOfRouting) {
                        // unit routed successfully
                        routedOpposingUnits.add(opposingUnit);
                        enemyUnits.remove(opposingUnit);
                        skirmishEnd = true;
                        break;
                    }
                }


            }

            if (enemyUnits.size() <= 0) {
                // engaging Army won
                // move engaging units into invaded province
                for (Unit routedUnit : routedEngagingUnits) {
                    routedUnit.setBroken(false);
                    humanUnits.add(routedUnit);
                }
                for (Building building : enemyProvince.getBuildings()) {
                    if (building.is("Training")) {
                        building.stopTrainingUnit();
                    }
                }
                enemyProvince.changeProvinceOwnership(humanFaction);
                humanProvince.moveUnits(enemyProvince);
                enemyProvince.lockDownProvince();
                return "Won battle!";

            } else if (humanUnits.size() <= 0) {
                // enemy won
                // return routed units to original province
                for (Unit routedUnit : routedEngagingUnits) {
                    routedUnit.setBroken(false);
                    humanUnits.add(routedUnit);
                }
                routedEngagingUnits.clear();
                for (Unit routedUnit : routedOpposingUnits) {
                    routedUnit.setBroken(false);
                    enemyUnits.add(routedUnit);
                }
                routedOpposingUnits.clear();
                return "Lost battle";

            }
            
        } 
        return "Drew battle!";
    }

    private int meleeEngagement(Unit engagingUnit, Unit opposingUnit) {
        if (engagingUnit.isBroken()) {
            return 0;
        }
        Random rand = new Random();
        double damageToOpposing;
        // engagingUnit damage
        damageToOpposing = (double)(opposingUnit.getNumTroops() * 0.1) * (((double) engagingUnit.getAttack())/ ((double)(opposingUnit.getArmour() + opposingUnit.getDefenseSkill()))) * (rand.nextGaussian() + 1);
        
        int casualties = (int) (damageToOpposing);
        if (casualties > opposingUnit.getNumTroops()) {
            casualties = opposingUnit.getNumTroops();
        } else if (casualties < 0 ) {
            casualties = 0;
        }

        return casualties;
    }

    private int rangedEngagement(Unit engagingUnit, Unit opposingUnit) {
        if (engagingUnit.isBroken()) {
            return 0;
        }
        if (engagingUnit.getAttackType().equals("melee")) {
            // melee units cannot inflict damage in ranged engagement
            return 0;
        }
        Random rand = new Random();
        double damageToOpposing;
        // engagingUnit damage
        damageToOpposing = (double)(opposingUnit.getNumTroops() * 0.1) * (((double) engagingUnit.getAttack())/ ((double)(opposingUnit.getArmour() + opposingUnit.getDefenseSkill()))) * (rand.nextGaussian() + 1);

        int casualties = (int) (opposingUnit.getNumTroops() * damageToOpposing);
        if (casualties > opposingUnit.getNumTroops()) {
            casualties = opposingUnit.getNumTroops();
        }
        
        return casualties;
    }
    
}
