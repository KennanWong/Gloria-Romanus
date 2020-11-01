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
 * @invar treasury > 0
 */
public class Faction {
    private String name;
    private Map<String, Province> provinces;
    private int treasury;
    private int numMaxLevelMines;
    private double buildingCostReductionMultiplier;
    private double soldierCostReductionMultiplier;

    public Faction (String name) {
        this.name = name;
        this.provinces = new HashMap<String, Province>();
        this.treasury = 500;
        this.buildingCostReductionMultiplier = 1;
        this.soldierCostReductionMultiplier = 1;
        this.numMaxLevelMines = 0;
    }

    
    public void collectTaxes() {
        for (Province province : provinces.values()) {
            int tax = (int) Math.round(province.getTaxRate()*province.getWealth());
            int newProvinceWealth = province.getWealth() - tax;
            if (newProvinceWealth < 0) {
                province.setWealth(0);
                setTreasury(getTreasury() + province.getWealth());
            } else {
                province.setWealth(newProvinceWealth);
                setTreasury(getTreasury() + tax);
            }
        }
    }
    

    public String getName() {
        return name;
    }

    public void addProvince(Province province) {
        provinces.put(province.getName(), province);
        province.setFaction(this);
    }

    public void removeProvince(Province province) {
        provinces.remove(province.getName());
        province.setFaction(null);
    }

    public ArrayList<String> getProvincesList () {
        ArrayList<String> provinceList = new ArrayList<>();
        for (Map.Entry province: provinces.entrySet()) {
            provinceList.add((String) province.getKey());
        }
        return provinceList;
    }
    public Map<String, Province> getProvinces() {
        return provinces;
    }

    public Province getProvince(String name) {
        return provinces.get(name);
    }

    public int getTreasury() {
        return treasury;
    }

    /** 
     * @pre value rounded with Math.round() && greater than 0
     */
    public void setTreasury(int gold) {
        this.treasury = gold;
    }

    public int getNumMaxLevelMines() {
        return numMaxLevelMines;
    }

    public void setNumMaxLevelMines(int numMaxLevelMines) {
        this.numMaxLevelMines = numMaxLevelMines;
    }

    public double getBuildingCostReductionMultiplier() {
        return buildingCostReductionMultiplier;
    }

    public void setBuildingCostReductionMultiplier(double buildingCostReductionMultiplier) {
        this.buildingCostReductionMultiplier = buildingCostReductionMultiplier;
    }

    public double getSoldierCostReductionMultiplier() {
        return soldierCostReductionMultiplier;
    }

    public void setSoldierCostReductionMultiplier(double soldierCostReductionMultiplier) {
        this.soldierCostReductionMultiplier = soldierCostReductionMultiplier;
    }
}

