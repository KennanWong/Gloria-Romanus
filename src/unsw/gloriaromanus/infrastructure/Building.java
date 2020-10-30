package unsw.gloriaromanus.infrastructure;

//import java.lang.Math; 

import java.io.IOException;
//import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public abstract class Building {
    private int cost;
    private int buildTime;
    private boolean built;
    private int level;
    private String type;
    private String status; // Possible statuses: Being built, Training, Idle
    private Province province; // the province the building is from
    private Unit unitBeingTrained; // the unit being trained in this building

    // gets called after every turn, and the building lowers it's build time
    public void update() {
        if (built) {
            // If the building is built and if there are any units that have finished
            // training
            if (unitBeingTrained != null) {
                if ((unitBeingTrained.getTurnsToTrain() - 1) < 0) {
                    // unit has finished training
                    province.addUnit(unitBeingTrained);
                    status = "Idle";
                    unitBeingTrained = null;
                } else {
                    int turnsLeft = unitBeingTrained.getTurnsToTrain() - 1;
                    unitBeingTrained.setTurnsToTrain(turnsLeft);
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
        // for UI
        String buildingConfigurationContent = Files
                .readString(Paths.get("src/unsw/gloriaromanus/infracstructure/building_configuration.json"));
        JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);
        JSONObject buildingType = buildingConfiguration.getJSONObject(this.type);
        JSONArray level = buildingType.getJSONArray("level");
        JSONObject j = level.getJSONObject(this.level + 1);

        return j;
    }

    public void upgrade() {
        // actually upgrade the building to a higher level
        this.level++;
        this.built = false;
        this.buildTime = 2;
    }

    /*
     * public int getCost() { return cost; }
     * 
     * public void setCost(int cost) { this.cost = cost; }
     */

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
        JSONArray level = buildingType.getJSONArray("level");
        JSONObject buildStats = level.getJSONObject(0);
        // int cost = Integer.parseInt(buildStats.getString("cost"));
        this.cost = (int) Math.round(cost*costMultiplier);
        int buildTime = buildStats.getInt("buildTime");
        this.type = type;
        this.buildTime = buildTime - buildTimeReduction;
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
        // building.put("cost", cost);

        return building;
    }

    public void setBuildingFromJSON(JSONObject json) throws JSONException, IOException {
        // type = json.getString("type");
        level = json.getInt("level");
        built = json.getBoolean("built");
        status = json.getString("status");
        if (status.equals("Training")) {
            JSONObject unitBeingTrainedJSON = json.getJSONObject("unitBeingTrained");
            unitBeingTrained = new Unit(unitBeingTrainedJSON.getString("type"), unitBeingTrainedJSON.getInt("numTroops"));
            unitBeingTrained.setUnitFromJSON(unitBeingTrainedJSON);
        } 
        // cost = json.getInt("cost");

    }
}