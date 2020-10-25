package unsw.gloriaromanus;

import unsw.gloriaromanus.infrastructure.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class Province {
    private String name;
    private Faction faction;
    private Unit unit;
    private ArrayList<Building> allBuildings;
    private ArrayList<TroopBuilding> troopBuildings;
    //private ArrayList<WealthBuilding> wealthBuildings;

    // \/ temporary just to ensure implementation is correct
    private int numTroops;

    public Province (String name, Faction faction) {
        this.name = name;
        this.faction = faction;
        Random r = new Random();
        this.numTroops = r.nextInt(500);
    }

    /**
     * Method to set the faction of a provine
     * It will first remove the province from the old faction,
     * then it will add the province to the provided faction
     * and set the faction of the province to the provided faction
     * @param faction Faction that we want to set
     */
    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Faction getFaction() {
        return faction;
    }

    public String getName() {
        return name;
    }

    public int getNumTroops() {
        return numTroops;
    }

    public void setNumTroops(int numTroops) {
        this.numTroops = numTroops;
    }

    public void addNumTroops(int numTroops) {
        this.numTroops += numTroops;
    }

    public void changeProvinceOwnership(Faction newOwner) {
        Faction currentOwner = faction;
        currentOwner.removeProvince(this);
        newOwner.addProvince(this);
    }

    //Functionality for buildings

    /**
     * The user will select a building on the UI to create 
     * 
     * @param building
     * @return
     */
    public String addBuilding(String building) throws IOException {
        //check if any building is currently being constructed in this province
        if (underConstruction()) {
            String s = "A building is already being constructed!";
            return s;
        }

        //TODO
        // check if any of our buildings will have a reduced cost time or build time
        // cost is a multiplier and buildtime is a constant turn reduction
        double costReduction = 1;
        int buildTimeReduction = 0;

        //create the building and add it to our lists of buildings
        switch (building) {
            case "Infantry":
                Infantry i = new Infantry(costReduction, buildTimeReduction);
                this.allBuildings.add(i);
                this.troopBuildings.add(i);
                break;
            case "Cavalry":
                Cavalry c = new Cavalry(costReduction, buildTimeReduction);
                this.allBuildings.add(c);
                this.troopBuildings.add(c);
                break;
            case "Artillery":
                Artillery a = new Artillery(costReduction, buildTimeReduction);
                this.allBuildings.add(a);
                this.troopBuildings.add(a);
                break;
            /*
            case "Mine":
                
                break;
            case "Farm":
                
                break;
            case "Port":
                
                break;
            case "Market":
                
                break;
            case "Wall":

                break;
            */
        }

        String s = "Construction began sucessfully!";
        return s;
    }

    private boolean underConstruction() {
        for (Building building : this.allBuildings) {
            if (building.getBuildTime() != 0) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        //update the province accordingly per turn
        //incomplete
        for (Building building : this.allBuildings) {
            if (building.isBuilt()) {
                continue;
            } else {
                building.update();
            }
        }
    }




}
