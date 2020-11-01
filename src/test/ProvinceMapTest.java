package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
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
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.json.JSONArray;
import org.json.JSONObject;


import unsw.gloriaromanus.*;

public class ProvinceMapTest {
    @Test
    public void initialisationTest() throws IOException {
        String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
    
        // get the adjacency matrix
        String provinceAdjacencyContent = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
        JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);
    
        // create the game map
        ProvinceMap provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");

        // Check factions
        assert(provinceMap.getFaction("Rome").getName().equals("Rome"));
        assert(provinceMap.getFaction("Gaul").getName().equals("Gaul"));

        // Check initial ownerships
        Faction rome = provinceMap.getFaction("Rome");
        Faction gaul = provinceMap.getFaction("Gaul");
        
        assert(provinceMap.getProvince("Lusitania").getFaction() == rome);
        assert(provinceMap.getProvince("Achaia").getFaction() == gaul);
    }

    @Test
    public void getMovementPointsReqTest() throws IOException {
        String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
    
        // get the adjacency matrix
        String provinceAdjacencyContent = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
        JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);
    
        // create the game map
        ProvinceMap provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");
        Province lusitania = provinceMap.getProvince("Lusitania");
        Province tarraconesis = provinceMap.getProvince("Tarraconesis");
        assert(provinceMap.getRequiredMovementPoints(lusitania, tarraconesis) == 4);
        
        Province numidia = provinceMap.getProvince("Numidia");
        assert(provinceMap.getRequiredMovementPoints(lusitania, numidia) == - 1);

        Province narbonensis = provinceMap.getProvince("Narbonensis");
        assert(provinceMap.getRequiredMovementPoints(lusitania, narbonensis) == 8);
        
    }

    @Test
    public void provinceConnectedTest() throws IOException {
        String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
    
        // get the adjacency matrix
        String provinceAdjacencyContent = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
        JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);
    
        // create the game map
        ProvinceMap provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");
        assert(provinceMap.confirmIfProvincesConnected("Lusitania", "Tarraconesis"));
        assert(!provinceMap.confirmIfProvincesConnected("Lusitania", "Numedia"));

    }

    @Test
    public void saveAndLoadTest() throws IOException {
        String intialOwnershipContent = Files.readString(Paths.get("bin/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
    
        // get the adjacency matrix
        String provinceAdjacencyContent = Files.readString(Paths.get("bin/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
        JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);
    
        // create the game map
        ProvinceMap provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");
        Province lusitania = provinceMap.getProvince("Lusitania");
        Province tarraconesis = provinceMap.getProvince("Tarraconesis");

        lusitania.moveUnits(tarraconesis);
        assert(tarraconesis.getUnitOfType("Swordsmen").getNumTroops() == 100);

        provinceMap.saveGame(0);

        provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");
        assert(tarraconesis.getUnitOfType("Swordsmen").getNumTroops() == 50);

        provinceMap.loadGame();

        assert(tarraconesis.getUnitOfType("Swordsmen").getNumTroops() == 100);

    }
}
