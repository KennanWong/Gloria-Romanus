package unsw.gloriaromanus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;


public class ProvinceMap {
    private Map<String, Province> provinces;
    private Map<String, Faction> factions;
    private JSONObject provinceAdjacencyMatrix;
    private int numCities = 0;

    public ProvinceMap (JSONObject map, JSONObject provinceAdjacencyMatrix) throws IOException {
        // take the JSON map
        // each key will be a faction, create a Faction for each
        // each value will be a province, create a province, and add it to the faction
        
        // Map<String, String> m = new HashMap<String, String>();

        this.provinces = new HashMap<String, Province>();
        this.factions = new HashMap<String, Faction>();
        for (String key : map.keySet()) {
        // key will be the faction name
            Faction faction = new Faction(key);
            this.factions.put(faction.getName(), faction);
            JSONArray ja = map.getJSONArray(key);
            // value is province name
            for (int i = 0; i < ja.length(); i++) {
                String provinceName = ja.getString(i);
                Province province = new Province(provinceName, faction);
                faction.addProvince(province);
                provinces.put(provinceName, province);
                numCities += 1;
            }
        }
        this.provinceAdjacencyMatrix = provinceAdjacencyMatrix;
    }

    public boolean confirmIfProvincesConnected(String province1, String province2) throws IOException {
        /*
        String content = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
        JSONObject provinceAdjacencyMatrix = new JSONObject(content);
        */
        return provinceAdjacencyMatrix.getJSONObject(province1).getBoolean(province2);
    }

    public Province getProvince(String name) {
        return provinces.get(name);
    }

    public Faction getFaction(String name) {
        return factions.get(name);
    }
    
    public int getRequiredMovementPoints(Province province1, Province province2) throws IOException {
        // since it is an adjacency matrix, dijkstras algo

        // two arrays - one to hold the distance travelled
        //            - one to hold the city it came from
        // we must also ensure that they can only move through province in which they have conquered

        Faction faction = province1.getFaction();

        Map<String,String> lastProvince = new HashMap<>();
        Map<String, Integer> distanceFromInit = new HashMap<>();

        for (String key: provinceAdjacencyMatrix.keySet()) {
            lastProvince.put(key, null);
            distanceFromInit.put(key, 0);
        }

        Queue<String> toCheck = new LinkedList<String>();
        toCheck.add(province1.getName());

        while (!toCheck.isEmpty()) {
            String checking = toCheck.remove();
            JSONObject connectionArray = provinceAdjacencyMatrix.getJSONObject(checking);
            for (String key: connectionArray.keySet()) {
                if (connectionArray.getBoolean(key) && getProvince(key).getFaction() == faction) {
                    // if there is a connection
                    // add to the lsit of places to check
                    if (distanceFromInit.get(key) == 0 || 
                        (distanceFromInit.get(checking) + getProvince(key).getMovementPointsReq() < distanceFromInit.get(key))){
                        toCheck.add(key);
                        lastProvince.put(key, checking);
                        distanceFromInit.put(key, (distanceFromInit.get(checking) + getProvince(key).getMovementPointsReq()));
                    }
                }
            }

        }


        if (distanceFromInit.get(province2.getName()) != 0) {
            return distanceFromInit.get(province2.getName());
        } else {
            return -1;
        }
    }
}
