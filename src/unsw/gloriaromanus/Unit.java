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
    private int numTroops;  // the number of troops in this unit (should reduce based on depletion)
    private String category;
    private int range;  // range of the unit
    private int armour;  // armour defense
    private int morale;  // resistance to fleeing
    private int speed;  // ability to disengage from disadvantageous battle
    private int attack;  // can be either missile or melee attack to simplify. Could improve implementation by differentiating!
    private int defenseSkill;  // skill to defend in battle. Does not protect from arrows!
    private int shieldDefense; // a shield

    public Unit(String name, int numTroops) throws IOException {
        // TODO = obtain these values from the file for the unit
        String unitConfigurationContent = Files.readString(Paths.get("src/unsw/gloriaromanus/unit_configuration.json"));
        JSONObject unitConfiguration = new JSONObject(unitConfigurationContent);
        JSONObject unitStat = unitConfiguration.getJSONObject(name);
        this.numTroops = numTroops;
        this.category = unitStat.getString("category");
        this.range = Integer.parseInt(unitStat.getString("range"));
        armour = Integer.parseInt(unitStat.getString("armour"));
        morale = Integer.parseInt(unitStat.getString("morale"));
        speed = Integer.parseInt(unitStat.getString("speed"));
        attack = Integer.parseInt(unitStat.getString("attack"));
        defenseSkill = Integer.parseInt(unitStat.getString("defenseSkill"));
        shieldDefense = Integer.parseInt(unitStat.getString("shieldDefense"));
    }

    public int getNumTroops(){
        return numTroops;
    }
    
    
}
