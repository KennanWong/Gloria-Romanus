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
import org.json.JSONObject;


/**
 * A base class for every type of building we have
 */
public abstract class Building {
    //private int cost;
    private int buildTime; 
    private boolean built; 
    private int level;
    private String type;
    private int turnNumber;
    private String status;      // Possible statuses: Being built, Training, Idle

    //gets called after every turn, and the building lowers it's build time
    public void update() {
        if (built) {
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
        //return the cost and buildtime of the next building level
        //for UI
        String buildingConfigurationContent = Files.readString(Paths.get("src/unsw/gloriaromanus/infracstructure/building_configuration.json"));
        JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);
        JSONObject buildingType = buildingConfiguration.getJSONObject(this.type);
        JSONArray level = buildingType.getJSONArray("level");
        JSONObject j = level.getJSONObject(this.level + 1);
 
        return j;
    }

    public void upgrade() {
        //actually upgrade the building to a higher level
        this.level++;
        this.built = false;
        this.buildTime = 2;
    }

    /*
    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
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

    public int getTurnNumber() {
        return turnNumber;
    }
    
    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public int getTurnAvailable() {
        return turnNumber + buildTime;
    }

    public Building(String type, double costMultiplier, int buildTimeReduction, int turnNumber) throws IOException {
        String buildingConfigurationContent = Files.readString(Paths.get("src/unsw/gloriaromanus/configFiles/building_configuration.json"));
        JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);
        JSONObject buildingType = buildingConfiguration.getJSONObject(type);
        JSONArray level = buildingType.getJSONArray("level");
        JSONObject buildStats = level.getJSONObject(0);
        //int cost = Integer.parseInt(buildStats.getString("cost");
        //this.cost = Math.round(cost*costMultiplier);
        int buildTime = buildStats.getInt("buildTime");
        this.type = type;
        this.buildTime = buildTime - buildTimeReduction;
        this.level = 0;
        this.built = false;
        status = "Being built";
    }



}
