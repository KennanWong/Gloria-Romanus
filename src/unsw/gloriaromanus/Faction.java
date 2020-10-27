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

public class Faction {
    private String name;
    private Map<String, Province> provinces;

    public Faction (String name) {
        this.name = name;
        this.provinces = new HashMap<String, Province>();
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
}
