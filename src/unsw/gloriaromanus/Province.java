package unsw.gloriaromanus;

<<<<<<< HEAD
import unsw.gloriaromanus.economy.*;
=======
>>>>>>> infrastructure
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
import org.json.JSONException;
import org.json.JSONObject;



public class Province {
    private String name;
    private Faction faction;
    private List<Unit> units;
    private String roadLevel;
    private int movementPointsReq;
    private boolean locked = false;
    private ArrayList<Building> allBuildings;
    private ArrayList<TroopBuilding> troopBuildings;
    private ProvinceWealth provinceWealth; //total wealth of the province
    private int growth; //gold gained per turn
    private String taxLevel;
    //private ArrayList<WealthBuilding> wealthBuildings;

    // \/ temporary just to ensure implementation is correct
    private int numTroops;

    public Province(String name, Faction faction) throws IOException {
        this.name = name;
        this.faction = faction;
        Random r = new Random();
        this.units = new ArrayList<>();
        
        Unit firstUnit = new Unit("Swordsmen", r.nextInt(500));
        units.add(firstUnit);
        
        this.roadLevel = "No roads";
        this.movementPointsReq = 4;
        allBuildings = new ArrayList<>();
        troopBuildings = new ArrayList<>();
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
        units.add(newUnit);
    }

    public void addNumTroops(int numTroops) {
        this.numTroops += numTroops;
    }

    public void lockDownProvince() {
        locked = true;
    }

    public void unlockProvince() {
        locked = false;
    }

    public String getRoadLevel() {
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

    public void setRoadLevel(String roadLevel) {
        this.roadLevel = roadLevel;
        switch (roadLevel) {
            case "No roads":
                this.movementPointsReq = 4;
                break;
            case "Dirt roads":
                this.movementPointsReq = 3;
                break;
            case "Paved roads":
                this.movementPointsReq = 2;
                break;
            case "Highways roads":
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
            Unit inNewProvince = toProvince.getUnitOfType(unit.getType());
            if (inNewProvince != null) {
                inNewProvince.setNumTroops(unit.getNumTroops() + inNewProvince.getNumTroops());
            } else {
                toProvince.addUnit(unit);
            }
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
        for (Building building : allBuildings) {
            
        }

        provinceJSON.put("units", unitsJSON);
        provinceJSON.put("roadLevel", roadLevel);
        provinceJSON.put("movementPointsReq", movementPointsReq);
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

        // set the infrastructure of the units
        roadLevel = json.getString("roadLevel");
        movementPointsReq = json.getInt("movementPointsReq");
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
<<<<<<< HEAD
        //TODO
        //if taxrate was changed, implement accordingly

=======
        //incomplete
>>>>>>> infrastructure
        for (Building building : this.allBuildings) {
            if (building.isBuilt()) {
                continue;
            } else {
                building.update();
            }
        }
<<<<<<< HEAD

        //Different tax rates have different effects
        
    }

    public void recruitSoldier() {
        //TODO
=======
>>>>>>> infrastructure
    }




}
