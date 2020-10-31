package unsw.gloriaromanus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProvinceMap extends Observer {
    private Map<String, Province> provinces;
    private Map<String, Faction> factions;
    private JSONObject provinceAdjacencyMatrix;
    private int numCities = 0;

    public ProvinceMap (JSONObject map, JSONObject provinceAdjacencyMatrix, String mode) throws IOException {
        // take the JSON map
        // each key will be a faction, create a Faction for each
        // each value will be a province, create a province, and add it to the faction
        
        // Map<String, String> m = new HashMap<String, String>();

        // have a mode to determine whether or not we are loading default map, or loading from a save

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

    public void addProvince(Province province) {
        provinces.put(province.getName(), province);
    }

    public void addFaction(Faction faction) {
        factions.put(faction.getName(), faction);
    }
    
    public int getRequiredMovementPoints(Province province1, Province province2) throws IOException {
        // since it is an adjacency matrix, dijkstras algo

        // two arrays - one to hold the distance travelled
        //            - one to hold the city it came from
        // we must also ensure that they can only move through province in which they have conquered

        Faction faction = province1.getFaction();

        Map<String, Boolean> haveVisitied = new HashMap<>();
        Map<String, Integer> distanceFromInit = new HashMap<>();

        for (String key: provinceAdjacencyMatrix.keySet()) {
            haveVisitied.put(key, false);
            distanceFromInit.put(key, 0);
        }

        Queue<String> toCheck = new LinkedList<String>();
        toCheck.add(province1.getName());

        while (!toCheck.isEmpty()) {
            String checking = toCheck.remove();
            JSONObject connectionArray = provinceAdjacencyMatrix.getJSONObject(checking);
            for (String key: connectionArray.keySet()) {
                if (connectionArray.getBoolean(key) && getProvince(key).getFaction() == faction && !haveVisitied.get(key)) {
                    // if there is a connection
                    // add to the lsit of places to check
                    if (distanceFromInit.get(key) == 0 || 
                        (distanceFromInit.get(checking) + getProvince(key).getMovementPointsReq() < distanceFromInit.get(key))){
                        toCheck.add(key);
                        haveVisitied.put(key, true);
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

    /**
     * Saves the game state to a json
     * @param turnCounter turn counter of the game in which we are saving
     * @throws IOException
     */
    public void saveGame(int turnCounter) throws IOException {
        /*
        Faction
            Province
                List<Units>
                List<Buildings>
        Create the jsonObject in each step and loop in
        once you exit a loop and it to the previous jsonobject
        */
        JSONObject gameSaveJSON = new JSONObject();
        for (String factionName : factions.keySet()) {
            Faction faction = factions.get(factionName);
            JSONObject factionJSON = new JSONObject();
            JSONObject provincesJSON = new JSONObject();
            Map<String, Province> provinces = faction.getProvinces();
            for (String provinceName: provinces.keySet()) {
                provincesJSON.put(provinceName, getProvince(provinceName).getProvinceAsJSON());
            }
            factionJSON.put("provinces", provincesJSON);
            factionJSON.put("user", faction.getUser());
            gameSaveJSON.put(factionName, factionJSON);
        }
        gameSaveJSON.put("turnCounter", turnCounter);
        String gameSaveContent = gameSaveJSON.toString();
        Path fileName = Path.of("src/unsw/gloriaromanus/game_save.json");
        Files.writeString(fileName, gameSaveContent);
    }


    public int loadGame() throws IOException {
        // clear factions
        // clear provinces
        factions.clear();
        provinces.clear();
        String gameSaveData = Files.readString(Path.of("src/unsw/gloriaromanus/game_save.json"));
        JSONObject gameSaveJSON = new JSONObject(gameSaveData);
        for (String factionName: gameSaveJSON.keySet()) {
            // For each faction create new faction object
            if (factionName.equals("turnCounter")) {
                break;
            }
            JSONObject factionJSON = gameSaveJSON.getJSONObject(factionName);
            Faction faction = new Faction(factionName);
            faction.setUser(factionJSON.getInt("user"));
            factions.put(factionName, faction);
            JSONObject provincesJSON = factionJSON.getJSONObject("provinces");
            for (String provinceName : provincesJSON.keySet()) {
                // for each province in the faction , create province object
                JSONObject provinceJSON = provincesJSON.getJSONObject(provinceName);
                Province province = new Province(provinceName, faction);
                province.setProvinceFromJSON(provinceJSON);
                provinces.put(provinceName, province);
                faction.addProvince(province);
            }
        }
        return gameSaveJSON.getInt("turnCounter");
    }

    public Faction checkWinner() {
        Faction tmp = null;
        for (String province : provinces.keySet()) {
            if (tmp == null) {
                tmp = getProvince(province).getFaction();
            } else if (tmp != getProvince(province).getFaction()) {
                return null;
            }
        }
        return tmp;
    }

    @Override
    public void update() {
        for (Province province : provinces.values()) {
            province.update();
        }
    }

    public Map<String, Faction> getFactions() {
        return factions;
    }
}
