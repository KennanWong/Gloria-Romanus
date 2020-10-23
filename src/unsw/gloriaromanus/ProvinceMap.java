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


public class ProvinceMap {
    private Map<String, Province> provinces;
    private Map<String, Faction> factions;
    private JSONObject provinceAdjacencyMatrix;


    public ProvinceMap (JSONObject map, JSONObject provinceAdjacencyMatrix) {
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
    
}
