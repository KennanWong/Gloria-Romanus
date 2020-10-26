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
    private List<Unit> units;
    private String roadLevel;
    private int movementPointsReq;

    // \/ temporary just to ensure implementation is correct
    private int numTroops;

    public Province (String name, Faction faction) throws IOException {
        this.name = name;
        this.faction = faction;
        Random r = new Random();
        this.units = new ArrayList<>();
        Unit firstUnit = new Unit("Swordsmen", r.nextInt(500));
        units.add(firstUnit);
        this.roadLevel = "No roads";
        this.movementPointsReq = 4;
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

    public List<Unit> getUnits() {
        return units;
    }

    public Unit getUnitOfType(String type) {
        for (Unit unit: units) {
            if (unit.getName() == type) {
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
     * @param toProvince province you want to move the troops too
     */
    public void moveUnits(Province toProvince) {
        List<Unit> currUnits = this.units;
        for (Unit unit: currUnits) {
            Unit inNewProvince = toProvince.getUnitOfType(unit.getName());
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
        for (Unit unit: units) {
            if (movementPt == 0 || unit.getMovementPoints() < movementPt) {
                movementPt = unit.getMovementPoints();
            }
        }

        return movementPt;
    }
}
