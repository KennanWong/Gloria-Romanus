package unsw.gloriaromanus;

//import java.lang.Math; 

import java.io.IOException;
//import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.util.JSONPObject;

/*
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
*/
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import unsw.gloriaromanus.*;

/**
 * A base class for every type of building we have
 */
public class Building {
    private int cost;
    private int buildTime;
    private boolean built;
    private int level;
    private String type;
    private String status; // Possible statuses: Being built, Training, Idle, broken
    private Province province; // the province the building is from
    private Unit unitBeingTrained; // the unit being trained in this building
    private JSONArray buildingConfig;
    private double bonus;

    // gets called after every turn, and the building lowers it's build time
    public void update() {
        if (built) {
            // If the building is built and if there are any units that have finished
            // training
            if (unitBeingTrained != null) {
                if (unitBeingTrained.getTurnsToTrain() <= 0) {
                    // unit has finished training
                    province.addUnit(unitBeingTrained);
                    status = "Idle";
                    unitBeingTrained = null;
                } else {
                    unitBeingTrained.update();
                }
            }
            return;
        } else {
            buildTime -= 1;
        }
        if (buildTime == 0) {
            built = true;
            status = "Idle";
        }

    }

    public JSONObject checkUpgrade() throws IOException {
        // return the cost and buildtime of the next building level
        JSONObject j = buildingConfig.getJSONObject(this.level + 1);

        return j;
    }

    public void upgrade() {
        // actually upgrade the building to a higher level
        if (level < 4) {
            level++;
            built = false;
            buildTime = buildingConfig.getJSONObject(level).getInt("buildTime");
        }
        
        
    }

    
    public int getCost() { 
        return cost; 
    }
    
    public void setCost(int cost) { 
        this.cost = cost; 
    }
    

    public int getBuildTime() {
        return buildTime;
    }

    public int getLevel() {
        return level;
    }

    public boolean isBuilt() {
        return this.built;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public int getTurnAvailable() {
        return buildTime;
    }

    public Building(String type, double costMultiplier, int buildTimeReduction, Province province) throws IOException {
        String buildingConfigurationContent = Files
                .readString(Paths.get("src/unsw/gloriaromanus/configFiles/building_configuration.json"));
        JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);

        JSONObject buildingType = buildingConfiguration.getJSONObject(type);
        JSONArray buildingConfig = buildingType.getJSONArray("level");
        JSONObject buildStats = buildingConfig.getJSONObject(0);
        this.cost = (int) Math.round(buildStats.getInt("cost")*costMultiplier);
        this.buildTime = buildStats.getInt("buildTime") - buildTimeReduction;
        this.type = type;
        this.level = 0;
        this.built = false;
        this.province = province;

        status = "Being built";
    }

    public void trainingUnit(Unit newUnit) {
        unitBeingTrained = newUnit;
        status = "Training";
        return;
    }

    public Unit getUnitBeingTrained() {
        return unitBeingTrained;
    }

    /**
     * Method to get the building as a JSON object
     * 
     * @return
     */
    public JSONObject getBuildingAsJSON() {
        JSONObject building = new JSONObject();
        building.put("type", type);
        building.put("level", level);
        building.put("built", built);
        building.put("status", status);
        if (unitBeingTrained != null) {
            building.put("unitBeingTrained", unitBeingTrained.getUnitAsJson());
        } 
        
        building.put("buildTime", buildTime);
        building.put("cost", cost);

        return building;
    }

    public void setBuildingFromJSON(JSONObject json) throws JSONException, IOException {
        type = json.getString("type");
        level = json.getInt("level");
        built = json.getBoolean("built");
        status = json.getString("status");
        if (status.equals("Training")) {
            JSONObject unitBeingTrainedJSON = json.getJSONObject("unitBeingTrained");
            unitBeingTrained = new Unit(unitBeingTrainedJSON.getString("type"), unitBeingTrainedJSON.getInt("numTroops"));
            unitBeingTrained.setUnitFromJSON(unitBeingTrainedJSON);
        } 
        cost = json.getInt("cost");

    }

    public double getBonus() {
        return bonus;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public boolean is(String status) {
        if (this.status.equals(status)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Method to set the status of a building
     * "Being built"   "Idle"    "Trainig"    "Broken"
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Removes the unit being trained
     */
    public void stopTrainingUnit() {
        unitBeingTrained = null;
        this.status = "Idle";
    }
}
