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
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
import org.json.JSONException;
import org.json.JSONObject;

import unsw.gloriaromanus.Commands.*;

public class GloriaRomanusController {

  @FXML
  private MapView mapView;
  @FXML
  private TextField invading_province;
  @FXML
  private TextField opponent_province;
  @FXML
  private TextArea output_terminal;
  @FXML
  private TextArea province_info_terminal;
  @FXML
  private TextField turn_number;
  @FXML
  private TextField treasury;

  private ArcGISMap map;

  private Map<String, String> provinceToOwningFactionMap;

  private Map<String, Integer> provinceToNumberTroopsMap;

  // private String humanFaction;

  private Stage stage; // the controllers stage;
  // private ArrayList<Faction> factions;

  private Faction user1;
  private Faction user2;

  private Faction humanFaction;
  private Faction enemyFaction;

  private int gold;
  // provinceMap object
  private ProvinceMap provinceMap;

  @FXML
  private TextField province_1;
  @FXML
  private TextField province_2;
  @FXML
  private TextField building_type;
  @FXML
  private TextField troop_field; // use this field for the type of troops and number of troops
  @FXML
  private TextField tax_level;

  @FXML
  private ComboBox soldier_options; // combobox to show all possible soldiers that can be recruited
  @FXML
  private ComboBox number_soldiers; // combobox to show number of soldiers that can be recruited
  @FXML
  private ComboBox building_options; // combobox to show possible buildigns that can be built

  @FXML
  private VBox province_pane;
  @FXML
  private VBox buildings_pane;         // pane used to display information about a province

  private Feature currentlySelectedProvince1;
  private Feature currentlySelectedProvince2;

  private FeatureLayer featureLayer_provinces;

  private int turnCounter = 0;    // to keep track of turn counter

  private boolean gameFinished = false;

  private List<Province> lockedProvinces;

  @FXML
  private void initialize() throws JsonParseException, JsonMappingException, IOException {
    // TODO = you should rely on an object oriented design to determine ownership
    // get the initial ownership json
    String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
    JSONObject ownership = new JSONObject(intialOwnershipContent);

    // get the adjacency matrix
    String provinceAdjacencyContent = Files.readString(Paths.get("src/unsw/gloriaromanus/province_adjacency_matrix_fully_connected.json"));
    JSONObject provinceAdjacencyMatrix = new JSONObject(provinceAdjacencyContent);

    // create the game map
    provinceMap = new ProvinceMap(ownership, provinceAdjacencyMatrix);
    lockedProvinces = new ArrayList<>();

    String factionAssignmentContent = Files.readString(Paths.get("src/unsw/gloriaromanus/faction_assignment.json"));
    JSONObject factionAssignmentJSON = new JSONObject(factionAssignmentContent);
    for (String factionName : factionAssignmentJSON.keySet()) {
      if (factionAssignmentJSON.getInt(factionName) == 1) {
        user1 = provinceMap.getFaction(factionName);
        provinceMap.getFaction(factionName).setUser(1);
      }
      if (factionAssignmentJSON.getInt(factionName) == 2) {
        user2 = provinceMap.getFaction(factionName);
        provinceMap.getFaction(factionName).setUser(2);
      }
    }


    printMessageToTerminal("Player1 : " + user1.getName());
    printMessageToTerminal("Player2: " + user2.getName());
    printMessageToTerminal("It is player1's turn");
    turn_number.setText(Integer.toString(turnCounter));
    treasury.setText(Integer.toString(user1.getTreasury()));
    setFactions();
    initializeProvinceLayers();
    
  }


  @FXML
  public void clickedInvadeButton(ActionEvent e) throws IOException {
    if (gameFinished) {
      return;
    }
    if (currentlySelectedProvince1 != null && currentlySelectedProvince2 != null){
      Province humanProvince = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
      Province enemyProvince = provinceMap.getProvince((String)currentlySelectedProvince2.getAttributes().get("name"));
      if (humanProvince.getFaction() != humanFaction || enemyProvince.getFaction() == humanFaction){
        printMessageToTerminal("Incorrect selection of provinces!\n If you would like to invade a province,\n select your province for Province 1 and \n select an enemy province for Province 2");
        resetSelections();  // reset selections in UI
        return;
      }
      if (provinceMap.confirmIfProvincesConnected(humanProvince.getName(), enemyProvince.getName())) {
        Command newCommand = new Command();
        newCommand.setStrategy(new Invade());
        printMessageToTerminal(newCommand.executeStrategy(humanProvince, enemyProvince));
      } else {
        printMessageToTerminal("Provinces not adjacent, cannot invade!");
        resetSelections();  // reset selections in UI
        return;
      }
      resetView();
      
    }
  }

  @FXML
  public void clickedMoveButton(ActionEvent e) throws IOException {
    if (gameFinished) {
      return;
    }
    setFactions();
    if (currentlySelectedProvince1 == null || currentlySelectedProvince2 == null) {
      printMessageToTerminal("Please select two provinces");
      resetSelections();  // reset selections in UI
      return;
    }
    Province moveFrom = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
    Province moveTo = provinceMap.getProvince((String)currentlySelectedProvince2.getAttributes().get("name"));

    if (moveFrom.getFaction() != humanFaction || moveTo.getFaction() != humanFaction) {
      printMessageToTerminal("Please select two provinces which are part of your faction");
      printMessageToTerminal("province1 faction - " + moveFrom.getFaction().getName());
      printMessageToTerminal("province2 faction - " + moveTo.getFaction().getName());
      printMessageToTerminal("humanFaction - " + humanFaction.getName());
      resetSelections();  // reset selections in UI
      return;
    }

    if (moveFrom.isLocked()) {
      printMessageToTerminal("Cannot move units from a province invaded in current turn");
      resetSelections();  // reset selections in UI
      return;
    }

    int movementPointsAvailable = moveFrom.getMovementPointsOfUnits();
    int requiredMovementPoints = provinceMap.getRequiredMovementPoints(moveFrom, moveTo);
    if (requiredMovementPoints == -1) {
      printMessageToTerminal("Unable to find path to requested province");
      resetSelections();
      return;
    }
    else if (requiredMovementPoints > movementPointsAvailable) {
      printMessageToTerminal("Not enough movement points to move troops");
      printMessageToTerminal("requiredMovementPoints - " + requiredMovementPoints);
      printMessageToTerminal("movementPointsAvaialble - " + movementPointsAvailable);
      resetSelections();
      return;
    }

    // Checked requirements, good to make move, add move to command queue
    Command newCommand = new Command();
    newCommand.setStrategy(new Move());
    printMessageToTerminal(newCommand.executeStrategy(moveFrom, moveTo));
    resetView();
  }

  @FXML
  public void clickedRaidButton(ActionEvent e) throws IOException {
    if (currentlySelectedProvince1 == null || currentlySelectedProvince2 == null) {
      printMessageToTerminal("Please select two provinces");
      resetSelections();  // reset selections in UI
      return;
    }
    Province engagingProvince = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
    Province defendingProvince = provinceMap.getProvince((String)currentlySelectedProvince2.getAttributes().get("name"));
    Command newCommand = new Command();
    newCommand.setStrategy(new Raid(humanFaction, enemyFaction));
    printMessageToTerminal(newCommand.executeStrategy(engagingProvince, defendingProvince));
    resetView();
    return ;
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
    // allocate factions
    for (Faction faction : provinceMap.getFactions().values()) {
      if (faction.getUser() == 1) {
        user1 = faction;
      }
      if (faction.getUser() == 2) {
        user2 = faction;
      }
    }

    setFactions();
    resetView();
    printMessageToTerminal("Loaded game from save!");
    printMessageToTerminal("Player 1: " + user1.getName());
    printMessageToTerminal("Player 2: " + user2.getName());
    printMessageToTerminal("It is currently player" + (turnCounter%2 + 1) + "'s turn.");
    turn_number.setText(Integer.toString(turnCounter));

  }

  @FXML
  public void clickedResetButton(ActionEvent e) throws IOException {
    resetView();
  }

  @FXML
  public void clickedEndTurnButton(ActionEvent e) throws IOException {
    endTurn();
  }

  


  @FXML 
  public void clickedAddBuildingButton(ActionEvent e) throws IOException {
    if (currentlySelectedProvince1 == null) {
      printMessageToTerminal("Please select a province");
      resetView();
      return;
    }
    
    setFactions();
    Province province = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
    String buildingType = (String) building_options.getValue();
    if (province.getFaction() != humanFaction) {
      printMessageToTerminal("Unable to add a building in a province you do not own!");
      return;
    }
    if (buildingType == null) {
      printMessageToTerminal("Please select a building type to build");
      return;
    }
    printMessageToTerminal(province.addBuilding(buildingType));
    gold = province.getFaction().getTreasury();
    treasury.setText(Integer.toString(gold));
    resetView();
  }
  @FXML
  public void clickedTaxationButton(ActionEvent e) throws IOException {
    if (currentlySelectedProvince1 == null) {
      resetView();
      return;
    }
    String[] options = {"Low", "Medium", "High", "Very High"};
    boolean validOption = false;
    String s = tax_level.getText();
    for (String string : options) {
      if (string.equals(s)){
        validOption = true;
      }
    }
    if (!validOption) {
      printMessageToTerminal("Invalid tax level request");
      printMessageToTerminal("Please enter one of the following options: ");
      printMessageToTerminal("Low|Medium|High|Very High");
    } else {
      Province province = provinceMap.getProvince((String)currentlySelectedProvince1.getAttributes().get("name"));
      province.setTaxLevel(s);
      printMessageToTerminal("When you end your turn, this province shall be taxed");
      printMessageToTerminal(province.getTaxRate()*100 + "% every year onwards");
      printMessageToTerminal("The province will also have: ");
      printMessageToTerminal(province.getGrowth() + " growth");
      if (s.equals("Very High")) {
        printMessageToTerminal("and -1 soldier morale");
      }

    }
    return;
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
              else if (features.size() == 1) {
                // note maybe best to track whether selected...
                Feature f = features.get(0);
                String provinceName = (String)f.getAttributes().get("name");
                Province province = provinceMap.getProvince(provinceName);
                if (currentlySelectedProvince1 == null) {
                  currentlySelectedProvince1 = f;
                  province_1.setText(province.getName());
                  displaySelection(currentlySelectedProvince1);
                  featureLayer.selectFeature(f);         
                } else if (provinceName.equals((String)currentlySelectedProvince1.getAttributes().get("name"))) {
                  province_1.setText("");
                  featureLayer.unselectFeature(currentlySelectedProvince1);
                  currentlySelectedProvince1 = null;
                } else if (currentlySelectedProvince2 == null) {
                  currentlySelectedProvince2 = f;
                  province_2.setText(province.getName());
                  displaySelection(currentlySelectedProvince2);
                  featureLayer.selectFeature(f);         
                } else if (provinceName.equals((String)currentlySelectedProvince2.getAttributes().get("name"))) {
                    province_2.setText("");
                    featureLayer.unselectFeature(currentlySelectedProvince2);
                    currentlySelectedProvince2 = null;
                  
                }
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
    if (currentlySelectedProvince1 != null && currentlySelectedProvince2 != null) {
      featureLayer_provinces.unselectFeatures(Arrays.asList(currentlySelectedProvince1, currentlySelectedProvince2));
      currentlySelectedProvince1 = null;
      currentlySelectedProvince2 = null;
      province_1.setText("");
      province_2.setText("");
    } else if (currentlySelectedProvince1 != null) {
      featureLayer_provinces.unselectFeatures(Arrays.asList(currentlySelectedProvince1));
      currentlySelectedProvince1 = null;
      currentlySelectedProvince2 = null;
      province_1.setText("");
      province_2.setText("");
    } else if (currentlySelectedProvince2 != null){
      featureLayer_provinces.unselectFeatures(Arrays.asList(currentlySelectedProvince2));
      currentlySelectedProvince1 = null;
      currentlySelectedProvince2 = null;
      province_1.setText("");
      province_2.setText("");
    }
    province_info_terminal.clear();
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

  /**
   * Method to end the current turn and move onto the next turn
   * This should also update all the provinces and check whether or not a building is built and update 
   * accordingly. it should also check for if there are any troops that have finished training
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  private void endTurn() throws JsonParseException, JsonMappingException, IOException {
    if (provinceMap.checkWinner() != null) {
      Faction winningFaction = provinceMap.checkWinner();
      printMessageToTerminal("GAME END\n" +"Winner: " + winningFaction.getName());
      gameFinished = true;
    }

    resetView();

    //Update all the provinces
    for (Province province : lockedProvinces) {
      province.unlockProvince();
    }

    provinceMap.update();
    user1.collectTaxes();
    user2.collectTaxes();
    lockedProvinces.clear();
    turnCounter++;
    humanFaction = null;
    enemyFaction = null;
    printMessageToTerminal("The year is " + turnCounter + " - now it is player" + (turnCounter % 2 + 1) + "'s turn");
    setFactions();
    treasury.setText(Integer.toString(humanFaction.getTreasury()));
    turn_number.setText(Integer.toString(turnCounter));
  }

  
  public void selectTaxLevel(Feature selectedProvince) {

  }

  public void displaySelection(Feature selectedProvince) {
    province_pane.getChildren().clear();
    clearDropDowns();
    if (selectedProvince == null) {
      return;
    }
    Province province = provinceMap.getProvince((String)selectedProvince.getAttributes().get("name"));
    if (province.getFaction() == humanFaction) {
      try {
        loadBuildingDropDown(province);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    province_pane.getChildren().add(province_info_terminal);

    // Add button upgrade roads
    if (province.getRoadLevel() < 3 && humanFaction.getTreasury() >= province.getCostToUpgrade()) {
      Button upgradeRoadsButton = new Button ("Upgrade Roads");
      province_pane.getChildren().add(upgradeRoadsButton);
      upgradeRoadsButton.setOnAction(new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent arg0) {
          printMessageToTerminal(province.upgradeRoads());
          resetView();
          return;
        }
      });
    }
    province_pane.getChildren().add(building_options);
    province_pane.getChildren().add(buildings_pane);
    province_info_terminal.clear();
    province_info_terminal.appendText(province.getName() + "\n");
    province_info_terminal.appendText("Tax Level: " + province.getTaxLevel() + "\n");
    province_info_terminal.appendText("Wealth: " + province.getWealth() + "\n");
    province_info_terminal.appendText("Road level: " + province.getRoadLevelString() + "\n");
    province_info_terminal.appendText("Units: \n");
    for (Unit unit: province.getUnits()) {
      province_info_terminal.appendText("\t" + unit.getType() + ": " + unit.getNumTroops() + " troops\n");
    }
    buildings_pane.getChildren().clear();
    buildings_pane.getChildren().add(new TextField("Buildings"));
    for (Building building: province.getBuildings()) {
      displayBuildingOptions(building);
    }
  }
  private void setFactions() {
    if (turnCounter%2 == 0) {
      this.humanFaction = user1;
      this.enemyFaction = user2;
    } else {
      this.humanFaction = user2;
      this.enemyFaction = user1;
    }
  }

  // Load up the possible soldiers that can be recurited

  private void loadBuildingDropDown(Province province) throws IOException {
    building_options.getItems().clear();
    building_options.setVisibleRowCount(5);
    String buildingConfigurationContent = Files
                .readString(Paths.get("src/unsw/gloriaromanus/configFiles/building_configuration.json"));
    JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);
    for (String buildingType : buildingConfiguration.keySet()) {
      JSONObject buildingJSON = buildingConfiguration.getJSONObject(buildingType).getJSONArray("level").getJSONObject(0);
      if (!province.buildingPresent(buildingType) && humanFaction.getTreasury() >= buildingJSON.getInt("cost")) {
        building_options.getItems().add(buildingType);
      }
    }
  }

  private void clearDropDowns() {
    building_options.getItems().clear();
    buildings_pane.getChildren().clear();
  }

  private void displayBuildingOptions(Building building) {
    Province province = building.getProvince();
    TextField buildingName = new TextField (building.getType() + " - Level " + (building.getLevel()+1));
    buildingName.setEditable(false);
    buildings_pane.getChildren().add(buildingName);

    if (province.getFaction() != humanFaction) {
      TextField buildingStatus = new TextField (building.getStatus());
      buildingStatus.setEditable(false);
      buildings_pane.getChildren().add(buildingStatus);
      return;
    }

    switch (building.getStatus()) {
      case "Being built":
        // text field stating that is being build
        TextField buildingStatus = new TextField (building.getStatus() + ", avaialble in "  
                                                  + building.getTurnAvailable() + " turns");
        buildingStatus.setEditable(false);
        buildings_pane.getChildren().add(buildingStatus);
        break;
      case "Idle":
        // provide option to recruit troops
        HBox trainingOptions = new HBox();
        ComboBox<String> soldierOptions = new ComboBox<String>();
        ComboBox<String> numberSoldiers=  new ComboBox<String>();
        trainingOptions.getChildren().add(soldierOptions);
        trainingOptions.getChildren().add(numberSoldiers);
        String unitConfigurationContent = null;
        try {
          unitConfigurationContent = Files
              .readString(Paths.get("src/unsw/gloriaromanus/configFiles/unit_configuration.json"));
        } catch (IOException e) {
          e.printStackTrace();
        }
        JSONObject unitConfiguration = new JSONObject(unitConfigurationContent);
        List<String> recruitableSoldierTypes = new ArrayList<>();
        for (String unitKey : unitConfiguration.keySet()) {
          JSONObject unit = unitConfiguration.getJSONObject(unitKey);
          if (unit.getString("category").equals(building.getType())) {
            recruitableSoldierTypes.add(unitKey);
          }
        }
        soldierOptions.getItems().addAll(recruitableSoldierTypes);
        soldierOptions.setOnAction(e -> {
          String chosenUnit = (String) soldierOptions.getValue();
          if (chosenUnit == null) {
            return;
          }
          JSONObject requestedUnitJSON = unitConfiguration.getJSONObject(chosenUnit);
          numberSoldiers.getItems().clear();
          numberSoldiers.setVisibleRowCount(5);
          int numRequestible = humanFaction.getTreasury()/requestedUnitJSON.getInt("cost");
          for (int i = 1; i <= numRequestible; i++) {
            numberSoldiers.getItems().add(Integer.toString(i));
          }
        });

        // Button to recruit soldiers
        Button recruitButton = new Button("Recruit Soldiers");
        trainingOptions.getChildren().add(recruitButton);
        recruitButton.setOnAction(new EventHandler<ActionEvent>(){
          @Override
          public void handle(ActionEvent arg0) {
            String soldierType = soldierOptions.getValue();
            int numSoldiers = Integer.parseInt(numberSoldiers.getValue());
            try {
              printMessageToTerminal(province.recruitSoldier(soldierType, numSoldiers));
            } catch (IOException e) {
              e.printStackTrace();
            }
            treasury.setText(Integer.toString(province.getFaction().getTreasury()));
            resetView();
            return;
          }
        });
        buildings_pane.getChildren().add(trainingOptions);

        // Add button to upgrade the building
        if (building.getLevel() < 3) {
          if (humanFaction.getTreasury() >= building.checkUpgrade().getInt("cost")) {
            Button upgradeButton = new Button("Upgrade this building");
            buildings_pane.getChildren().add(upgradeButton);
            upgradeButton.setOnAction(new EventHandler<ActionEvent>(){
              @Override
              public void handle(ActionEvent arg0) {
                printMessageToTerminal(building.upgrade());
                treasury.setText(Integer.toString(province.getFaction().getTreasury()));
                resetView();
              }
            });
          }
          
        }
        
        break;
      case "Training" :
        // text field stating that is currently training a troop and when it will finish training
        TextField trainingStatus = new TextField (building.getStatus() +" soldiers. Available in "
                                                  + building.getUnitBeingTrained().getTurnsToTrain());
        trainingStatus.setEditable(false);
        buildings_pane.getChildren().add(trainingStatus);
        break;
      case "Broken" :
        // provide option to rebuild the building at a cost
        TextField brokenStatus = new TextField ("Building has been destroyed!");
        brokenStatus.setEditable(false);
        buildings_pane.getChildren().add(brokenStatus);
        Button repairButton = new Button("Repair Building");
        buildings_pane.getChildren().add(repairButton);
        repairButton.setOnAction(new EventHandler<ActionEvent>(){
          @Override
          public void handle(ActionEvent arg0) {
            printMessageToTerminal(building.repair());
            treasury.setText(Integer.toString(province.getFaction().getTreasury()));
            resetView();
            return;
          }
        });
        break;
    }
  }
  
  private void resetView() {
    resetSelections();
    try {
      addAllPointGraphics();
    } catch (IOException e) {
      e.printStackTrace();
    }
    province_pane.getChildren().clear();
    treasury.setText(Integer.toString(humanFaction.getTreasury()));
    clearDropDowns();
  }
}
