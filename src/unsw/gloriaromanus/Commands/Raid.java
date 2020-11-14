package unsw.gloriaromanus.Commands;

import unsw.gloriaromanus.Province;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import unsw.gloriaromanus.*;

public class Raid implements Strategy {
    // Raid a province, to reap their wealth, destroy troop production buildings and damage enemy units, and then return back to home province

    Faction humanFaction;
    Faction enemyFaction;

    public Raid (Faction humanFaction, Faction enemyFaction) {
        this.humanFaction = humanFaction;
        this.enemyFaction = enemyFaction;
    }


    @Override
    public String execute(Province province1, Province province2) {
        if (province1.isLocked()) {
            return "Cannot use a province to raid another province in the same turn";
        }
        if (province1.getFaction() != humanFaction) {
            return "Cannot use a province which you do not own to invade another province!";
        }

        if (province2.getFaction() != enemyFaction) {
            return "Please select an enemy province to raid";
        }

        List<Unit> engagingUnits = province1.getSelectedUnits();
        if (engagingUnits.size() == 0) {
            return "Cannot raid another province with no units!";
        }
        List<Unit> opposingUnits = province2.getUnits();

        double sizeOfEngaging = 0.0;
        double engagingMorale = 0.0;
        for (Unit unit : engagingUnits) {
            sizeOfEngaging += unit.getNumTroops();
            engagingMorale += unit.getMorale();
        }
        double sizeOfDefending = 0.0;
        double defendingMorale = 0.0;
        for (Unit unit : province2.getUnits()) {
            sizeOfDefending += unit.getNumTroops();
            defendingMorale += unit.getMorale();
        }



        // Invasion battle resolver
        double chance = sizeOfEngaging* engagingMorale/(sizeOfEngaging* engagingMorale + sizeOfDefending * defendingMorale);
        double casualtiesMulti = sizeOfDefending * defendingMorale/(sizeOfEngaging* engagingMorale + sizeOfDefending * defendingMorale);

        Random rand = new Random();
        if (rand.nextDouble() < chance) {
            // sucessful raid
            for (Unit unit : opposingUnits) {
                int numCasualties = rand.nextInt((int)(((double) unit.getNumTroops())* chance));
                unit.setNumTroops(unit.getNumTroops() - numCasualties);
            }
            for (Unit unit : engagingUnits) {
                int numCasualties = rand.nextInt((int)(((double) unit.getNumTroops())* casualtiesMulti));
                unit.setNumTroops(unit.getNumTroops() - numCasualties);
            }
            List<Building> buildingsToBeRemoved  = new ArrayList<>();
            for (Building building : province2.getBuildings()) {
                if (building.is("Being built")) {
                    buildingsToBeRemoved.add(building);
                    continue;
                } else {
                    if (building.is("Training")) {
                        building.stopTrainingUnit();
                    } 
                    building.setStatus("Broken");
                }
                province2.setRaided(true);
            }
            for (Building building : buildingsToBeRemoved) {
                province2.getBuildings().remove(building);
            }
            province1.setWealth(province1.getWealth() + province2.getWealth());
            province2.setWealth(0);
            province2.setTaxLevel("High");
            province1.addUnits(engagingUnits);
            province1.lockDownProvince();
            
            return "Successful Raid!";

        } else {
            for (Unit unit : opposingUnits) {
                int bound = (int)(((double) unit.getNumTroops())* chance);
                if (bound == 0) {
                    bound = 1;
                }
                int numCasualties = rand.nextInt(bound);
                unit.setNumTroops(unit.getNumTroops() - numCasualties);
            }
            for (Unit unit : engagingUnits) {
                int bound = (int)(((double) unit.getNumTroops())* casualtiesMulti);
                if (bound == 0) {
                    bound = 1;
                }
                int numCasualties = rand.nextInt(bound);
                unit.setNumTroops(unit.getNumTroops() - numCasualties);
            } 
            province1.addUnits(engagingUnits);
            return Double.toString(casualtiesMulti);
        }
    }
    
}
