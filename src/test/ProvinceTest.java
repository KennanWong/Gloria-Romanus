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

import unsw.gloriaromanus.*;

public class ProvinceTest {
    @Test
    public void initialisationTest() throws IOException {
        Faction faction = new Faction("Rome");
        Province province = new Province("Lusitania", faction);
        assert(faction.getProvincesList().contains("Lusitania"));
        assert(province.getMovementPointsReq() == 4);
    }

    @Test
    public void getUnitOfTypeTest() throws IOException {
        Faction faction = new Faction("Rome");
        Province province = new Province("Lusitania", faction);
        assert(province.getUnitOfType("Swordsmen").getType().equals("Swordsmen"));
    }

    @Test
    public void addBuildingTest() throws IOException {
        Faction faction = new Faction("Rome");
        Province province = new Province("Lusitania", faction);
        province.addBuilding("Infantry");
        List<Building> buildings = province.getBuildings();
        
        // Check if the building has been added to the buildings list in the province
        boolean hasBuilding = false;
        for (Building building: buildings) {
            if (building.getType().equals("Infantry")){
                hasBuilding = true;
            }
        }
        assert(hasBuilding);
    }


    @Test
    public void recruitSoldierTest() throws IOException {
        Faction faction = new Faction("Rome");
        Province province = new Province("Lusitania", faction);
        province.addBuilding("Infantry");
        Building infantry = null;
        for (Building building: province.getBuildings()) {
            if (building.getType().equals("Infantry")) {
                infantry = building;
            }
        }
        assert (infantry != null);
        int buildTime = infantry.getBuildTime();
        for (int i = 0; i < buildTime; i++) {
            province.update();
        }
        province.recruitSoldier("Swordsmen", 10);
        int currentTroops = province.getUnitOfType("Swordsmen").getNumTroops();
        province.update();
        province.update();
        assert (province.getUnitOfType("Swordsmen").getNumTroops() == currentTroops + 10);
        
    }

    @Test
    public void movementOfSoldiersTest() throws IOException {
        Faction faction = new Faction("Rome");
        Province province1 = new Province("Lusitania", faction);
        Province province2 = new Province("Lusitania", faction);
        assert(province1.getUnitOfType("Swordsmen") != null);
        int troopsProvince1 = province1.getUnitOfType("Swordsmen").getNumTroops();
        assert(province2.getUnitOfType("Swordsmen") != null);
        int troopsProvince2 = province2.getUnitOfType("Swordsmen").getNumTroops();
        province1.moveUnits(province2);
        assert(province1.getUnitOfType("Swordsmen").getNumTroops() == troopsProvince1 + troopsProvince2);
        assert (province2.getUnitOfType("Swordsmen") == null);
    }




}
