package unsw.gloriaromanus;


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
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;




public class Province {
    private String name;
    private Faction faction;
    private List<Unit> units;
    private int roadLevel;
    private int movementPointsReq;
    private boolean locked = false;
    private ArrayList<Building> buildings;
    private int wealth; //total wealth of the province
    private String taxLevel; //low med high very high
    private int growth; //gold gained per turn
    private double taxRate; // a multiplier between 0 and 1
    private int numLoses;
    private boolean raided;     // flag to notify if a province has been raided that turn;


    // \/ temporary just to ensure implementation is correct
    private int numTroops;

    public Province(String name, Faction faction) throws IOException {
        this.name = name;
        this.faction = faction;
        this.units = new ArrayList<>();
        
        Unit firstUnit = new Unit("Swordsmen",50);
        units.add(firstUnit);
        
        this.roadLevel = 0;
        this.movementPointsReq = 4;
        buildings = new ArrayList<>();
        taxLevel = "Low";
        taxRate = 0.1;
        growth = 10;
        numLoses = 0;
    }

    /**
     * Method to set the faction of a provine It will first remove the province from
     * the old faction, then it will add the province to the provided faction and
     * set the faction of the province to the provided faction
     * 
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

    public List<Unit> getUnits() {
        return units;
    }

    public Unit getUnitOfType(String type) {
        for (Unit unit : units) {
            if (unit.getType().equals(type)) {
                return unit;
            }
        }
        return null;
    }

    public void setNumTroops(int numTroops) {
        this.numTroops = numTroops;
    }

    public void addUnit(Unit newUnit) {
        for (Unit unit: units) {
            if (unit.getType().equals(newUnit.getType())) {
                unit.setNumTroops(unit.getNumTroops() + newUnit.getNumTroops());
                return;
            } 
        }
        units.add(newUnit);
    }

    public void lockDownProvince() {
        locked = true;
    }

    

    public void unlockProvince() {
        locked = false;
    }

    public int getRoadLevel() {
        return roadLevel;
    }
    
    public boolean isLocked() {
        return locked;
    }

    public void changeProvinceOwnership(Faction newOwner) {
        Faction currentOwner = faction;
        currentOwner.removeProvince(this);
        newOwner.addProvince(this);
    }

    public void setRoadLevel(int roadLevel) {
        this.roadLevel = roadLevel;
        switch (roadLevel) {
            case 0:
                this.movementPointsReq = 4;
                break;
            case 1:
                this.movementPointsReq = 3;
                break;
            case 2:
                this.movementPointsReq = 2;
                break;
            case 3:
                this.movementPointsReq = 1;
                break;

        }
    }

    /**
     * Move all units from one province to another province
     * 
     * @param toProvince province you want to move the troops too
     */
    public void moveUnits(Province toProvince) {
        List<Unit> currUnits = this.units;
        for (Unit unit : currUnits) {
            toProvince.addUnit(unit);
        }
        units.clear();
    }

    public int getMovementPointsReq() {
        return movementPointsReq;
    }

    public int getMovementPointsOfUnits() {
        int movementPt = 0;
        for (Unit unit : units) {
            if (movementPt == 0 || unit.getMovementPoints() < movementPt) {
                movementPt = unit.getMovementPoints();
            }
        }
        return movementPt;
    }

    public JSONObject getProvinceAsJSON() {
        JSONObject provinceJSON = new JSONObject();
        provinceJSON.put("name", name);
        JSONArray unitsJSON = new JSONArray();
        for (Unit unit : units) {
            unitsJSON.put(unit.getUnitAsJson());
        }
        JSONArray buildingsJSON = new JSONArray();
        for (Building building : buildings) {
            buildingsJSON.put(building.getBuildingAsJSON());
        }
        provinceJSON.put("buildings", buildingsJSON);
        provinceJSON.put("units", unitsJSON);
        provinceJSON.put("roadLevel", roadLevel);
        provinceJSON.put("movementPointsReq", movementPointsReq);
        provinceJSON.put("wealth", wealth);
        provinceJSON.put("taxLevel", taxLevel);
        provinceJSON.put("taxRate", taxRate);
        provinceJSON.put("locked", locked);
        provinceJSON.put("growth", growth);
        return provinceJSON;
    }

    public void setProvinceFromJSON(JSONObject json) throws JSONException, IOException {
        units.clear();
        name = json.getString("name");
        // set the units of the province
        JSONArray unitsJSON = json.getJSONArray("units");
        for (int i = 0; i < unitsJSON.length(); i++) {
            JSONObject unitJSON = unitsJSON.getJSONObject(i);
            Unit unit = new Unit(unitJSON.getString("type"), unitJSON.getInt("numTroops"));
            unit.setUnitFromJSON(unitJSON);
            addUnit(unit);
        }
        JSONArray buildingsJSON = json.getJSONArray("buildings");
        for (int i = 0; i < buildingsJSON.length(); i++ ) {
            JSONObject buildingJSON = buildingsJSON.getJSONObject(i);
            addBuilding(buildingJSON.getString("type"));
            buildings.get(i).setBuildingFromJSON(buildingJSON);
        }

        // set the infrastructure of the units
        roadLevel = json.getInt("roadLevel");
        movementPointsReq = json.getInt("movementPointsReq");
        wealth = json.getInt("wealth");
        taxLevel = json.getString("taxLevel");
        taxRate = json.getDouble("taxRate");
        locked = json.getBoolean("locked");
        growth = json.getInt("growth");
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

        //check if we already have that same type of building here
        if (buildingPresent(building)) {
            String s = "You already have this type of building!";
            return s;
        }

        // check if any of our buildings will have a reduced cost time or build time
        double costReduction = faction.getBuildingCostReductionMultiplier();
        int buildTimeReduction = faction.getNumMaxLevelMines();

        // Check if we have been given a valid building type
        String buildingConfigurationContent = Files
                .readString(Paths.get("src/unsw/gloriaromanus/configFiles/building_configuration.json"));
        JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);


        Building b = new Building(building, costReduction, buildTimeReduction, this);
        
        
        if (b.getCost() > faction.getTreasury()) {
            return "Not enough money!";
        }

        
        buildings.add(b);
        faction.setTreasury(faction.getTreasury() - b.getCost());
        
        return "Construction began sucessfully!";
        
    }

    private boolean underConstruction() {
        for (Building building : this.buildings) {
            if (building.getBuildTime() != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean buildingPresent(String building) {
        boolean flag = false;
        for (Building b : getBuildings()) {
            if (b.getType().equals(building)) {
                flag = true;
            }
        }
        return flag;
    }

    public String upgradeRoads() {
        faction.setTreasury(faction.getTreasury() - getCostToUpgrade());
        roadLevel +=1;
        setRoadLevel(roadLevel);
        return "Successfully upgraded the roads!";
    }

    public String getRoadLevelString() {
        switch (roadLevel) {
            case 0:
                return "No Roads";
            case 1:
                return "Dirt Roads";
            case 2:
                return "Paved Roads";
            case 3:
                return "HighWay";
        }
        return null;
    }

    public int getCostToUpgrade() {
        switch (roadLevel+1) {
            case 1:
                return 200;
            case 2:
                return 300;
            case 3:
                return 400;
        }
        return 0;
    }
    
    /**
     * Method to update a province by applying the changes made to the province update building times, troop training and wealth generation
     */
    public void update() {
        raided = false;
        for (Building building : this.buildings) {
            building.update();
            if (building.is("Broken")) {
                raided = true;
            }
        }
        for (Unit unit : units) {
            unit.update();
        }
        
        locked = false;
        growWealth();      
    }

    public List<Building> getBuildings() {
        return buildings;
    }
    
    public void setTaxLevel(String taxLevel) {
        this.taxLevel = taxLevel;
        switch (taxLevel) {
            case "Low":
                growth = 10;
                taxRate = 0.10;
                break;
            case "Medium":
                growth = 0;
                taxRate = 0.15;
                break;
            case "High":
                growth = -10;
                taxRate = 0.20;
                break;
            case "Very High":
                growth = -30;
                taxRate = 0.25;
                for (Unit unit : this.units) {
                    unit.setMorale(unit.getMorale() - 1);
                }
                break;
        }
    }



    public String recruitSoldier(String unitType, int numTroops) throws IOException {
        Unit newUnit = new Unit(unitType, numTroops);  
        //check if we have the right building and building level to create this troop
        Building buildingAvailable = null;
        for (Building building : buildings) {
            if (building.getType().equals(newUnit.getCategory()) && building.isBuilt()) {
                buildingAvailable = building;
                break;
            }
        }
        //add the troop and substract the money
        // need to account for soldier training time
        newUnit.setTurnsToTrain(buildingAvailable);
        buildingAvailable.trainingUnit(newUnit);
        faction.setTreasury(faction.getTreasury() - newUnit.getCost()*newUnit.getNumTroops());

        String s = "You have successfully recruited " + newUnit.getNumTroops() + " " + newUnit.getCategory()
                    + "\n Will begin training.";
        return s;
    }

    private void growWealth() {
        int newProvinceWealth = getGrowth() + getWealth();
        if (newProvinceWealth < 0) {
            setWealth(0);
        } else {
            setWealth(newProvinceWealth);
        }
    }

    public int getWealth() {
        return wealth;
    }

    /** 
     * @pre value rounded with Math.round() && greater than 0
     */
    public void setWealth(int wealth) {
        this.wealth = wealth;
    }

    public int getGrowth() {
        return growth;
    }

    public void setGrowth(int growth) {
        this.growth = growth;
    }

    public String getTaxLevel() {
        return taxLevel;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public Building getBuilding(String buildingType) {
        for (Building building : buildings) {
            if (building.getType().equals(buildingType)) {
                return building;
            }
        }
        return null;
    }

    public List<Unit> getSelectedUnits() {
        List<Unit> selectedUnits = new ArrayList<>();
        List<Unit> toBeRemoved = new ArrayList<>();
        for (Unit unit : units) {
            if (unit.isSelected()) {
                Unit unitSelected = null;
                try {
                    if (unit.getNumSelected() == 0) {
                        // If a number of units arent declared, default to using the entire unit
                        unitSelected = new Unit(unit.getType(), unit.getNumTroops());
                        toBeRemoved.add(unit);
                    } else {
                        unitSelected = new Unit(unit.getType(), unit.getNumSelected());
                        unit.setNumTroops(unit.getNumTroops() - unit.getNumSelected());
                        unit.setSelected(false);
                        unit.setNumSelected(0);
                    }
                    selectedUnits.add(unitSelected);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Unit unit : toBeRemoved) {
            units.remove(unit);
        }
        return selectedUnits;
    }

    /**
     * Given a list of units, add them to the province
     * @param listOfUnits
     */
    public void addUnits(List<Unit> listOfUnits) {
        for (Unit unit : listOfUnits) {
            addUnit(unit);
        }
    }

    public boolean isRaided() {
        return raided;
    }

    public void setRaided(boolean raided) {
        this.raided = raided;
    }
}
