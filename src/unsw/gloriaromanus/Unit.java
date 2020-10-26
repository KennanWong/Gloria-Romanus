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
    private String name;        // name of troop
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

    /**
     * Constructor for a unit class. Provide the nameOfTroop and the number of those troops, and it will pull from a configuration
     * file to fill in the appropriate statistics
     * @param nameOfTroop Name of the troop we want a unit class of
     * @param numTroops Number of troops wanted in that unit
     * @throws IOException
     */
    public Unit(String nameOfTroop, int numTroops) throws IOException {
        String unitConfigurationContent = Files.readString(Paths.get("src/unsw/gloriaromanus/unit_configuration.json"));
        JSONObject unitConfiguration = new JSONObject(unitConfigurationContent);
        JSONObject unitStat = unitConfiguration.getJSONObject(nameOfTroop);
        this.numTroops = numTroops;
        this.category = unitStat.getString("category");
        name = nameOfTroop;

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

    public void setNumTroops(int numTroops) {
        this.numTroops = numTroops;
    }

    public String getName() {
        return name;
    }
}
