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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.data.GeoPackage;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol.HorizontalAlignment;
import com.esri.arcgisruntime.symbology.TextSymbol.VerticalAlignment;
import com.esri.arcgisruntime.data.Feature;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;

import org.json.JSONArray;
import org.json.JSONObject;

public class GloriaRomanusController{

  @FXML
  private MapView mapView;
  @FXML
  private TextField invading_province;
  @FXML
  private TextField opponent_province;
  @FXML
  private TextArea output_terminal;

  private ArcGISMap map;

  private Map<String, String> provinceToOwningFactionMap;

  private Map<String, Integer> provinceToNumberTroopsMap;

  // private String humanFaction;


  private Faction user1;
  private Faction user2;

  private Faction humanFaction;

  private Faction enemyFaction;

  // provinceMap object
  private ProvinceMap provinceMap;

  private Feature currentlySelectedHumanProvince;
  private Feature currentlySelectedEnemyProvince;


  @FXML
  private TextField province_1;
  @FXML
  private TextField province_2;

  private Feature currentlySelectedProvince1;
  private Feature currentlySelectedProvince2;

  private FeatureLayer featureLayer_provinces;

  private int selectionStep = 0;  // to keep track of selection
  private int turnCounter = 0;    // to keep track of turn counter

  private boolean gameFinished = false;

  @FXML
  private void initialize() throws JsonParseException, JsonMappingException, IOException {
    // TODO = you should rely on an object oriented design to determine ownership
    // get the initial ownership json
    String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
    JSONObject ownership = new JSONObject(intialOwnershipContent);

    // get the adjacency matrix
    String provinceAdjacencyContent = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
    JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);


    provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix, "new");

    /*
    provinceToOwningFactionMap = getProvinceToOwningFactionMap();

    provinceToNumberTroopsMap = new HashMap<String, Integer>();
    Random r = new Random();
    for (String provinceName : provinceToOwningFactionMap.keySet()) {
      provinceToNumberTroopsMap.put(provinceName, r.nextInt(500));
    }
    */
    // TODO = load this from a configuration file you create (user should be able to
    // select in loading screen)
    user1 = provinceMap.getFaction("Rome");
    user2 = provinceMap.getFaction("Gaul");

    currentlySelectedHumanProvince = null;
    currentlySelectedEnemyProvince = null;

    printMessageToTerminal("Player1 : Rome");
    printMessageToTerminal("Player2: Gaul");
    printMessageToTerminal("It is player1's turn");

    initializeProvinceLayers();
  }

  @FXML
  public void clickedInvadeButton(ActionEvent e) throws IOException {
    if (gameFinished) {
      return;
    }
    if (turnCounter%2 == 0) {
      humanFaction = user1;
      enemyFaction = user2;
    } else {
      humanFaction = user2;
      enemyFaction = user1;
    }

    if (currentlySelectedProvince1 != null && currentlySelectedProvince2 != null){
      /*
      String humanProvince = (String)currentlySelectedHumanProvince.getAttributes().get("name");
      String enemyProvince = (String)currentlySelectedEnemyProvince.getAttributes().get("name");
      */
      Province humanProvince = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
      Province enemyProvince = provinceMap.getProvince((String)currentlySelectedProvince2.getAttributes().get("name"));
      if (humanProvince.getFaction() != humanFaction || enemyProvince.getFaction() == humanFaction){
        printMessageToTerminal("Incorrect selection of provinces!\n If you would like to invade a province,\n select your province for Province 1 and \n select an enemy province for Province 2");
        resetSelections();  // reset selections in UI
        addAllPointGraphics(); // reset graphics
        return;
      }
      
      if (provinceMap.confirmIfProvincesConnected(humanProvince.getName(), enemyProvince.getName())){
        List <Unit> humanUnits = humanProvince.getUnits();
        List <Unit> enemyUnits = enemyProvince.getUnits();
      
        // army strength calculated as the sum of number of soldiers in unit x attack x defense for all units in the army.

        double humanStrength = 0;
        for (Unit unit: humanUnits) {
          humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (humanStrength == 0) {
          printMessageToTerminal("No soldiers to go into battle with!");
          resetSelections();  // reset selections in UI
          addAllPointGraphics(); // reset graphics
          return;
        }

        double enemyStrength = 0;
        for (Unit unit: enemyUnits) {
          enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
        }

        if (enemyStrength == 0) {
          // invade by default
          enemyProvince.changeProvinceOwnership(humanFaction);
          humanProvince.moveUnits(enemyProvince);
          endTurn();
          return;
        }

        boolean finished = false;
        int numEncounters = 0;
        while (!finished) {
          double chanceOfWinning = humanStrength/(humanStrength + enemyStrength);
          Random r = new Random();
          double chance = r.nextDouble();

          double winningStength = 0;
          double losingStrength = 0;
          List <Unit> winningUnits;
          List <Unit> losingUnits;
          if (chance < chanceOfWinning) {
            // human won
            printMessageToTerminal("Won encounter!");
            winningStength = humanStrength;
            losingStrength = enemyStrength;
            winningUnits = humanUnits;
            losingUnits = enemyUnits;
          } else {
            // enemy won
            printMessageToTerminal("Loss encounter");
            winningStength = enemyStrength;
            losingStrength = humanStrength;
            winningUnits = enemyUnits;
            losingUnits = humanUnits;
          }

          
          double percentageOfLoser = 0.0;

          while (percentageOfLoser == 0.0) {
            double tmp = r.nextDouble();
            if (tmp >= (winningStength)/(winningStength + losingStrength)) {
              percentageOfLoser = tmp;
            }
          }

          for (Unit unit: losingUnits) {
            unit.setNumTroops((int)((double)unit.getNumTroops()*(1-percentageOfLoser)));
          }

          humanStrength = 0;
          for (Unit unit: humanUnits) {
            humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
          }

          enemyStrength = 0;
          for (Unit unit: enemyUnits) {
            enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
          }
          if (enemyStrength == 0) {
            // human won
            enemyProvince.changeProvinceOwnership(humanFaction);
            humanProvince.moveUnits(enemyProvince);
            printMessageToTerminal("Won battle!");
            break;
          } 
          if (humanStrength == 0) {
            // enemy won
            humanProvince.changeProvinceOwnership(enemyProvince.getFaction());
            enemyProvince.moveUnits(humanProvince);
            printMessageToTerminal("Lost battle!");
            break;
          }

          double percentageOfWinner = 0.0;

          while (percentageOfWinner == 0.0) {
            double tmp = r.nextDouble();
            if (tmp <= (losingStrength)/(winningStength + losingStrength)) {
              percentageOfWinner = tmp;
            }
          }

          for (Unit unit: winningUnits) {
            unit.setNumTroops((int)((double) unit.getNumTroops()*(1-percentageOfWinner)));
            printMessageToTerminal("Winning units now " + unit.getNumTroops());
          }

          humanStrength = 0;
          for (Unit unit: humanUnits) {
            humanStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
          }

          enemyStrength = 0;
          for (Unit unit: enemyUnits) {
            enemyStrength += (unit.getNumTroops()*unit.getAttack()*unit.getDefenseSkill());
          }
          if (enemyStrength == 0) {
            // human won
            enemyProvince.changeProvinceOwnership(humanFaction);
            humanProvince.moveUnits(enemyProvince);
            printMessageToTerminal("Won battle!");
            
            break;
          } 
          if (humanStrength == 0) {
            // enemy won
            humanProvince.changeProvinceOwnership(enemyProvince.getFaction());
            enemyProvince.moveUnits(humanProvince);
            printMessageToTerminal("Lost battle!");
            break;
          }

          numEncounters += 1; 
          // recalculate strengths
          
          if (numEncounters == 200) {
            break;
          }
        }
        endTurn();
      } else {
        printMessageToTerminal("Provinces not adjacent, cannot invade!");
      }
    }
  }

  @FXML
  public void clickedMoveButton(ActionEvent e) throws IOException {
    if (gameFinished) {
      return;
    }
    if (turnCounter%2 == 0) {
      humanFaction = user1;
      enemyFaction = user2;
    } else {
      humanFaction = user2;
      enemyFaction = user1;
    }
    Province moveFrom = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
    Province moveTo = provinceMap.getProvince((String)currentlySelectedProvince2.getAttributes().get("name"));

    if (moveFrom.getFaction() != humanFaction || moveTo.getFaction() != humanFaction) {
      printMessageToTerminal("Please select two provinces which are part of your faction");
      printMessageToTerminal("province1 faction - " + moveFrom.getFaction().getName());
      printMessageToTerminal("province2 faction - " + moveTo.getFaction().getName());
      printMessageToTerminal("humanFaction - " + humanFaction.getName());
      resetSelections();  // reset selections in UI
      addAllPointGraphics(); // reset graphics
      return;
    }
    

    int movementPointsAvailable = moveFrom.getMovementPointsOfUnits();
    
    int requiredMovementPoints = provinceMap.getRequiredMovementPoints(moveFrom, moveTo);
    if (requiredMovementPoints == -1) {
      printMessageToTerminal("Unable to find path to requested province");
    }
    else if (requiredMovementPoints <= movementPointsAvailable) {
      moveFrom.moveUnits(moveTo);
    } else {
      printMessageToTerminal("Not enough movement points to move troops");
      printMessageToTerminal("requiredMovementPoints - " + requiredMovementPoints);
      printMessageToTerminal("movementPointsAvaialble - " + movementPointsAvailable);
    }
    turnCounter ++;
    enemyFaction = null;
    humanFaction = null;
    printMessageToTerminal("End turn - now it is player" + (turnCounter%2 + 1) + "'s turn");
    resetSelections();  // reset selections in UI
    addAllPointGraphics(); // reset graphics
  }

  @FXML
  public void clickedSaveButton(ActionEvent e) throws IOException {
    provinceMap.saveGame(turnCounter);
    printMessageToTerminal("Saved Game!");
    return;
  }

  @FXML
  public void clickedLoadButton(ActionEvent e) throws IOException {
    
    turnCounter = provinceMap.loadGame();
    user1 = provinceMap.getFaction(user1.getName());
    user2 = provinceMap.getFaction(user2.getName());
    addAllPointGraphics(); // reset graphics
    
    printMessageToTerminal("Loaded game from save!");
    printMessageToTerminal("It is currently player" + (turnCounter%2 + 1) + "'s turn.");

  }

  @FXML
  public void clickedResetButton(ActionEvent e) throws IOException {
    addAllPointGraphics(); // reset graphics
  }

  /**
   * run this initially to update province owner, change feature in each
   * FeatureLayer to be visible/invisible depending on owner. Can also update
   * graphics initially
   */
  private void initializeProvinceLayers() throws JsonParseException, JsonMappingException, IOException {

    Basemap myBasemap = Basemap.createImagery();
    // myBasemap.getReferenceLayers().remove(0);
    map = new ArcGISMap(myBasemap);
    mapView.setMap(map);

    // note - tried having different FeatureLayers for AI and human provinces to
    // allow different selection colors, but deprecated setSelectionColor method
    // does nothing
    // so forced to only have 1 selection color (unless construct graphics overlays
    // to give color highlighting)
    GeoPackage gpkg_provinces = new GeoPackage("src/unsw/gloriaromanus/provinces_right_hand_fixed.gpkg");
    gpkg_provinces.loadAsync();
    gpkg_provinces.addDoneLoadingListener(() -> {
      if (gpkg_provinces.getLoadStatus() == LoadStatus.LOADED) {
        // create province border feature
        featureLayer_provinces = createFeatureLayer(gpkg_provinces);
        map.getOperationalLayers().add(featureLayer_provinces);

      } else {
        System.out.println("load failure");
      }
    });

    addAllPointGraphics();
  }

  private void addAllPointGraphics() throws JsonParseException, JsonMappingException, IOException {
    mapView.getGraphicsOverlays().clear();

    InputStream inputStream = new FileInputStream(new File("src/unsw/gloriaromanus/provinces_label.geojson"));
    FeatureCollection fc = new ObjectMapper().readValue(inputStream, FeatureCollection.class);

    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    for (org.geojson.Feature f : fc.getFeatures()) {
      if (f.getGeometry() instanceof org.geojson.Point) {
        org.geojson.Point p = (org.geojson.Point) f.getGeometry();
        LngLatAlt coor = p.getCoordinates();
        Point curPoint = new Point(coor.getLongitude(), coor.getLatitude(), SpatialReferences.getWgs84());
        PictureMarkerSymbol s = null;
        Province province = provinceMap.getProvince((String) f.getProperty("name"));
        Faction faction = province.getFaction();

        String text = faction.getName() + "\n" + province.getName() + "\n";
        for (Unit unit: province.getUnits()) {
           text += unit.getType() + " - " + unit.getNumTroops() + "\n";
        }

        TextSymbol t = new TextSymbol(10,
            text, 0xFFFF0000,
            HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

        switch (faction.getName()){
          case "Gaul":
            // note can instantiate a PictureMarkerSymbol using the JavaFX Image class - so could
            // construct it with custom-produced BufferedImages stored in Ram
            // http://jens-na.github.io/2013/11/06/java-how-to-concat-buffered-images/
            // then you could convert it to JavaFX image https://stackoverflow.com/a/30970114

            // you can pass in a filename to create a PictureMarkerSymbol...
            s = new PictureMarkerSymbol(new Image((new File("images/Celtic_Druid.png")).toURI().toString()));
            break;
          case "Rome":
            // you can also pass in a javafx Image to create a PictureMarkerSymbol (different to BufferedImage)
            s = new PictureMarkerSymbol("images/legionary.png");
            break;
          // TODO = handle all faction names, and find a better structure...
        }
        t.setHaloColor(0xFFFFFFFF);
        t.setHaloWidth(2);
        Graphic gPic = new Graphic(curPoint, s);
        Graphic gText = new Graphic(curPoint, t);
        graphicsOverlay.getGraphics().add(gPic);
        graphicsOverlay.getGraphics().add(gText);
      } else {
        System.out.println("Non-point geo json object in file");
      }

    }

    inputStream.close();
    mapView.getGraphicsOverlays().add(graphicsOverlay);
  }

  private FeatureLayer createFeatureLayer(GeoPackage gpkg_provinces) {
    FeatureTable geoPackageTable_provinces = gpkg_provinces.getGeoPackageFeatureTables().get(0);

    // Make sure a feature table was found in the package
    if (geoPackageTable_provinces == null) {
      System.out.println("no geoPackageTable found");
      return null;
    }

    // Create a layer to show the feature table
    FeatureLayer flp = new FeatureLayer(geoPackageTable_provinces);

    // https://developers.arcgis.com/java/latest/guide/identify-features.htm
    // listen to the mouse clicked event on the map view
    mapView.setOnMouseClicked(e -> {
      // was the main button pressed?
      if (e.getButton() == MouseButton.PRIMARY) {
        // get the screen point where the user clicked or tapped
        Point2D screenPoint = new Point2D(e.getX(), e.getY());

        // specifying the layer to identify, where to identify, tolerance around point,
        // to return pop-ups only, and
        // maximum results
        // note - if select right on border, even with 0 tolerance, can select multiple
        // features - so have to check length of result when handling it
        final ListenableFuture<IdentifyLayerResult> identifyFuture = mapView.identifyLayerAsync(flp,
            screenPoint, 0, false, 25);

        // add a listener to the future
        identifyFuture.addDoneListener(() -> {
          try {
            // get the identify results from the future - returns when the operation is
            // complete
            IdentifyLayerResult identifyLayerResult = identifyFuture.get();
            // a reference to the feature layer can be used, for example, to select
            // identified features
            if (identifyLayerResult.getLayerContent() instanceof FeatureLayer) {
              FeatureLayer featureLayer = (FeatureLayer) identifyLayerResult.getLayerContent();
              // select all features that were identified
              List<Feature> features = identifyLayerResult.getElements().stream().map(f -> (Feature) f).collect(Collectors.toList());

              if (features.size() > 1){
                printMessageToTerminal("Have more than 1 element - you might have clicked on boundary!");
              }
              else if (features.size() == 1){
                // note maybe best to track whether selected...
                Feature f = features.get(0);
                String provinceName = (String)f.getAttributes().get("name");
                Province province = provinceMap.getProvince(provinceName);
                /*
                if (province.getFaction().equals(humanFaction)){
                  // province owned by human
                  if (currentlySelectedHumanProvince != null){
                    featureLayer.unselectFeature(currentlySelectedHumanProvince);
                  }
                  currentlySelectedHumanProvince = f;
                  invading_province.setText(province.getName());
                }
                else{
                  if (currentlySelectedEnemyProvince != null){
                    featureLayer.unselectFeature(currentlySelectedEnemyProvince);
                  }
                  currentlySelectedEnemyProvince = f;
                  opponent_province.setText(province.getName());
                }
                */
                if (selectionStep%2 == 0) {
                  if (currentlySelectedProvince1 != null) {
                    featureLayer.unselectFeature(currentlySelectedProvince1);
                  }
                  currentlySelectedProvince1 = f;
                  province_1.setText(province.getName());

                } else {
                  if (currentlySelectedProvince2 != null) {
                    featureLayer.unselectFeature(currentlySelectedProvince2);
                  }
                  currentlySelectedProvince2 = f;
                  province_2.setText(province.getName());
                }
                selectionStep += 1;

                

                featureLayer.selectFeature(f);                
              }

              
            }
          } catch (InterruptedException | ExecutionException ex) {
            // ... must deal with checked exceptions thrown from the async identify
            // operation
            System.out.println("InterruptedException occurred");
          }
        });
      }
    });
    return flp;
  }

  private Map<String, String> getProvinceToOwningFactionMap() throws IOException {
    String content = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
    JSONObject ownership = new JSONObject(content);
    Map<String, String> m = new HashMap<String, String>();
    for (String key : ownership.keySet()) {
      // key will be the faction name
      JSONArray ja = ownership.getJSONArray(key);
      // value is province name
      for (int i = 0; i < ja.length(); i++) {
        String value = ja.getString(i);
        m.put(value, key);
      }
    }
    return m;
  }

  private ArrayList<String> getHumanProvincesList() throws IOException {
    // https://developers.arcgis.com/labs/java/query-a-feature-layer/
    /*
    String content = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
    JSONObject ownership = new JSONObject(content);
    return ArrayUtil.convert(ownership.getJSONArray(humanFaction));
    */
    return humanFaction.getProvincesList();
  }

  /**
   * returns query for arcgis to get features representing human provinces can
   * apply this to FeatureTable.queryFeaturesAsync() pass string to
   * QueryParameters.setWhereClause() as the query string
   */
  private String getHumanProvincesQuery() throws IOException {
    LinkedList<String> l = new LinkedList<String>();
    for (String hp : getHumanProvincesList()) {
      l.add("name='" + hp + "'");
    }
    return "(" + String.join(" OR ", l) + ")";
  }

  private boolean confirmIfProvincesConnected(String province1, String province2) throws IOException {
    String content = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
    JSONObject provinceAdjacencyMatrix = new JSONObject(content);
    return provinceAdjacencyMatrix.getJSONObject(province1).getBoolean(province2);
  }

  private void resetSelections(){
    featureLayer_provinces.unselectFeatures(Arrays.asList(currentlySelectedProvince1, currentlySelectedProvince2));
    /*
    currentlySelectedEnemyProvince = null;
    currentlySelectedHumanProvince = null;
    invading_province.setText("");
    opponent_province.setText("");
    */
    selectionStep = 0;
    currentlySelectedProvince1 = null;
    currentlySelectedProvince2 = null;
    province_1.setText("");
    province_2.setText("");
  }

  private void printMessageToTerminal(String message){
    output_terminal.appendText(message+"\n");
  }

  /**
   * Stops and releases all resources used in application.
   */
  void terminate() {

    if (mapView != null) {
      mapView.dispose();
    }
  }

  private void endTurn() throws JsonParseException, JsonMappingException, IOException {
    if (provinceMap.checkWinner() != null) {
      Faction winningFaction = provinceMap.checkWinner();
      printMessageToTerminal("GAME END\n" +"Winner: " + winningFaction.getName());
      gameFinished = true;

    }
    turnCounter++;
    humanFaction = null;
    enemyFaction = null;
    printMessageToTerminal("End turn - now it is player" + (turnCounter%2 + 1) + "'s turn");
    resetSelections();  // reset selections in UI
    addAllPointGraphics(); // reset graphic
  }
}