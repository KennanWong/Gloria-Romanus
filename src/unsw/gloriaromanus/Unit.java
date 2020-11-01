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
import org.json.JSONObject;

/**
 * Represents a basic unit of soldiers
 * 
 * incomplete - should have heavy infantry, skirmishers, spearmen, lancers, heavy cavalry, elephants, chariots, archers, slingers, horse-archers, onagers, ballista, etc...
 * higher classes include ranged infantry, cavalry, infantry, artillery
 * 
 * current version represents a heavy infantry unit (almost no range, decent armour and morale)
 */
public class Unit {
    private String type;        // name of troop
    private int numTroops;      // the number of troops in this unit (should reduce based on depletion)
    private String category;    // the category of the soldier
    private int range;          // range of the unit
    private int armour;         // armour defense
    private int morale;         // resistance to fleeing
    private int speed;          // ability to disengage from disadvantageous battle
    private String attackType;  // attack type of the unit
    private int attack;         // can be either missile or melee attack to simplify. Could improve implementation by differentiating!
    private int defenseSkill;   // skill to defend in battle. Does not protect from arrows!
    private int shieldDefense;  // a shield
    private int movementPoints; // movement points of the unit
    private int cost;           // cost of the troop type
    private int level;          // building level that is required to produce this troop
    private int turnsToTrain;   // number of turns required to train this troop RN defaults to 1;
    /**
     * Constructor for a unit class. Provide the typeOfTroop and the number of those troops, and it will pull from a configuration
     * file to fill in the appropriate statistics
     * @param typeOfTroop type of the troop we want a unit class of
     * @param numTroops Number of troops wanted in that unit
     * @throws IOException
     */
    public Unit(String typeOfTroop, int numTroops) throws IOException {
        String unitConfigurationContent = Files.readString(Paths.get("src/unsw/gloriaromanus/configFiles/unit_configuration.json"));
        JSONObject unitConfiguration = new JSONObject(unitConfigurationContent);
        JSONObject unitStat = unitConfiguration.getJSONObject(typeOfTroop);
        this.numTroops = numTroops;
        this.category = unitStat.getString("category");
        type = typeOfTroop;

        switch (category) {
            case "Cavalry":
                movementPoints = 15;
                break;
        
            case "Artillery":
                movementPoints = 4;
                break;
            
            case "Infantry":
                movementPoints = 10;
                break;
        }

        range = unitStat.getInt("range");
        armour = unitStat.getInt("armour");
        morale = unitStat.getInt("morale");
        speed = unitStat.getInt("speed");
        attack = unitStat.getInt("attack");
        attackType = unitStat.getString("attackType");
        defenseSkill = unitStat.getInt("defenseSkill");
        shieldDefense = unitStat.getInt("shieldDefense");
        turnsToTrain = 1 * level;
        cost = unitStat.getInt("cost");
        level = unitStat.getInt("level");
    }

    public int getNumTroops(){
        return numTroops;
    }
    
    public int getMovementPoints() {
        return movementPoints;
    }
    
    public int getDefenseSkill() {
        return defenseSkill;
    }

    public int getAttack() {
        return attack;
    }

    public String getCategory() {
        return category;
    }

    public int getTurnsToTrain() {
        return turnsToTrain;
    }

    public void setTurnsToTrain(int turnsToTrain) {
        this.turnsToTrain = turnsToTrain;
    }

    public void setNumTroops(int numTroops) {
        this.numTroops = numTroops;
    }

    public String getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public int getMorale() {
        return morale;
    }
    public void setMorale(int morale) {
        this.morale = morale;
    }

    public int getLevel() {
        return level;
    }

    public void update() {
        if (getTurnsToTrain() != 0) {
            turnsToTrain--;
        }
    }

    public JSONObject getUnitAsJson() {
        JSONObject unitJSON = new JSONObject();
        unitJSON.put("type", type);
        unitJSON.put("numTroops", numTroops);
        unitJSON.put("category", category);
        unitJSON.put("range", range);
        unitJSON.put("armour", armour);
        unitJSON.put("morale", morale);
        unitJSON.put("speed", speed);
        unitJSON.put("attackType", attackType);
        unitJSON.put("attack", attack);
        unitJSON.put("defenseSkill", defenseSkill);
        unitJSON.put("shieldDefense", shieldDefense);
        unitJSON.put("movementPoints", movementPoints);
        unitJSON.put("cost", cost);
        unitJSON.put("level", level);
        return unitJSON;

    }

    public void setUnitFromJSON(JSONObject json) {
        category = json.getString("category");
        range = json.getInt("range");
        armour = json.getInt("armour");
        morale = json.getInt("morale");
        speed = json.getInt("speed");
        attackType = json.getString("attackType");
        attack = json.getInt("attack");
        defenseSkill = json.getInt("defenseSkill");
        shieldDefense = json.getInt("shieldDefense");
        movementPoints = json.getInt("movementPoints");
        cost = json.getInt("cost");
        level = json.getInt("level");
    }
}
