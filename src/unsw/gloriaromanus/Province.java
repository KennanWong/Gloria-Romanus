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

public class Province {
    private String name;
    private Faction faction;
    private Unit unit;

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
}
