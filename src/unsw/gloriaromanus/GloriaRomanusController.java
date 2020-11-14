package unsw.gloriaromanus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
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
import com.esri.arcgisruntime.symbology.TextSymbol.FontWeight;
import com.esri.arcgisruntime.symbology.TextSymbol.HorizontalAlignment;
import com.esri.arcgisruntime.symbology.TextSymbol.VerticalAlignment;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import unsw.gloriaromanus.Commands.Command;
import unsw.gloriaromanus.Commands.Invade;
import unsw.gloriaromanus.Commands.Move;
import unsw.gloriaromanus.Commands.Raid;

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
  private TextField turn_number;
  @FXML
  private TextField treasury;

  private ArcGISMap map;



  // private String humanFaction;

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
  private TextField current_player;         // textfield for the current player

  @FXML
  private ComboBox<String> soldier_options; // combobox to show all possible soldiers that can be recruited
  @FXML
  private ComboBox<String> number_soldiers; // combobox to show number of soldiers that can be recruited
  @FXML
  private ComboBox<String> building_options; // combobox to show possible buildigns that can be built

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
                  province_pane.getChildren().clear();
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
                    province_pane.getChildren().clear();
                  
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
    resetView();
    turn_number.setText(Integer.toString(turnCounter));
  }

  
  public void selectTaxLevel(Feature selectedProvince) {

  }

  public void displaySelection(Feature selectedProvince) {

    if (province_pane.getChildren() != null) {
      province_pane.getChildren().clear();
    }
    
    if (selectedProvince == null) {
      return;
    }
    // Get the selected province
    Province province = provinceMap.getProvince((String)selectedProvince.getAttributes().get("name"));

    // Add the information about the province
    TextArea provinceInfoTerminal = new TextArea();

    province_pane.getChildren().add(provinceInfoTerminal);

    provinceInfoTerminal.clear();
    provinceInfoTerminal.appendText(province.getName() + "\n");
    provinceInfoTerminal.appendText("Tax Level: " + province.getTaxLevel() + "\n");
    provinceInfoTerminal.appendText("Wealth: " + province.getWealth() + "\n");
    provinceInfoTerminal.appendText("Road level: " + province.getRoadLevelString() + "\n");
    provinceInfoTerminal.appendText("Units: \n");

    // Display buttons to modify the province if we are the owner of that province
    if (province.getFaction() == humanFaction) {

      // Add the options to add buildings to the selected province
      if (loadAddBuildingButton(province) != null) {
        province_pane.getChildren().add(loadAddBuildingButton(province));
      }

      // Add button upgrade roads
      if (addUpgradeRoadButton(province) != null) {
        province_pane.getChildren().add(addUpgradeRoadButton(province));
      }
      
    }
    TextField unitTF = new TextField("Units");
    unitTF.setEditable(false);
    unitTF.setAlignment(Pos.CENTER);
    unitTF.setFont(Font.font(unitTF.getFont().getName(), javafx.scene.text.FontWeight.BOLD, 13));
    province_pane.getChildren().add(unitTF);

    for (Unit unit: province.getUnits()) {
      if (province.getFaction() == humanFaction) {
        displayUnitOptions(unit, province_pane);
      } else {
        TextField unitInfo = new TextField(unit.getNumTroops() + " " + unit.getType());
        unitInfo.setEditable(false);
        province_pane.getChildren().add(unitInfo);
      }
    }

    TextField buildingTF = new TextField("Buildings");
    buildingTF.setEditable(false);
    buildingTF.setAlignment(Pos.CENTER);
    buildingTF.setFont(Font.font(buildingTF.getFont().getName(), javafx.scene.text.FontWeight.BOLD, 13));
    province_pane.getChildren().add(buildingTF);
;
    for (Building building: province.getBuildings()) {
      displayBuildingOptions(building, province_pane, 0);
    }
  }

  private void setFactions() {
    if (turnCounter%2 == 0) {
      this.humanFaction = user1;
      this.enemyFaction = user2;
      current_player.setText("Player 1 - " + humanFaction.getName());
    } else {
      this.humanFaction = user2;
      this.enemyFaction = user1;
      current_player.setText("Player 2 - " + humanFaction.getName());
    }
    
  }

  private void displayBuildingOptions(Building building, VBox vBox, int index) {
    VBox buildingBox = new VBox();
    Province province = building.getProvince();
    Faction faction = province.getFaction();
    TextField buildingName = new TextField (building.getType() + " - Level " + (building.getLevel()+1));
    buildingName.setEditable(false);
    buildingName.setAlignment(Pos.CENTER);
    buildingBox.getChildren().add(buildingName);

    if (province.getFaction() != humanFaction) {
      TextField buildingStatus = new TextField (building.getStatus());
      buildingStatus.setEditable(false);
      buildingBox.getChildren().add(buildingStatus);
      vBox.getChildren().add(buildingBox);
      return;
    }

    switch (building.getStatus()) {
      case "Being built":
        // Text field stating that it is being built
        TextField buildingStatus = new TextField (building.getStatus() + ", avaialble in "  
                                                  + building.getTurnAvailable() + " turns");
        buildingStatus.setEditable(false);
        buildingBox.getChildren().add(buildingStatus);
        break;
      case "Idle":
        // provide option to recruit troops
        HBox trainingOptions = new HBox();
        ComboBox<String> soldierOptions = new ComboBox<String>();
        ComboBox<String> numberSoldiers=  new ComboBox<String>();
        Button upgradeButton = new Button("Upgrade this building");
        Button recruitButton = new Button("Recruit Soldiers");
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

        // Load combo box for all recruitable soldier types
        List<String> recruitableSoldierTypes = new ArrayList<>();
        for (String unitKey : unitConfiguration.keySet()) {
          JSONObject unit = unitConfiguration.getJSONObject(unitKey);
          if (unit.getString("category").equals(building.getType()) && faction.getTreasury() >= unit.getInt("cost")) {
            recruitableSoldierTypes.add(unitKey);
          }
        }
        soldierOptions.getItems().addAll(recruitableSoldierTypes);

        // If a soldier type is selected, populate number of soldier ComboBox with the number they can upgrade
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

        numberSoldiers.setOnAction(e -> {
          // Create button to add soldiers once we have added the correct selections
          String chosenType = (String) soldierOptions.getValue();
          String numSoldiersStr = (String) numberSoldiers.getValue();
          if (chosenType == null || numSoldiersStr == null) {
            return;
          }
          int numSoldiers = Integer.parseInt(numSoldiersStr);
          if (!trainingOptions.getChildren().contains(recruitButton)) {
            trainingOptions.getChildren().add(recruitButton);
          }
          
          recruitButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent arg0) {
              try {
                printMessageToTerminal(province.recruitSoldier(chosenType, numSoldiers));
              } catch (IOException e) {
                e.printStackTrace();
              }
              treasury.setText(Integer.toString(province.getFaction().getTreasury()));
              int indexOf = vBox.getChildren().indexOf(buildingBox);
              vBox.getChildren().remove(buildingBox);
              displayBuildingOptions(building, vBox, indexOf);
              return;
            }
          });
        });
        
        buildingBox.getChildren().add(trainingOptions);
        

        // Add button to upgrade the building
        if (building.getLevel() < 3) {
          if (humanFaction.getTreasury() >= building.checkUpgrade().getInt("cost")) {
            buildingBox.getChildren().add(upgradeButton);
            upgradeButton.setOnAction(new EventHandler<ActionEvent>(){
              @Override
              public void handle(ActionEvent arg0) {
                printMessageToTerminal(building.upgrade());
                treasury.setText(Integer.toString(province.getFaction().getTreasury()));
                int indexOf = vBox.getChildren().indexOf(buildingBox);
                vBox.getChildren().remove(buildingBox);
                displayBuildingOptions(building, vBox, indexOf);
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
        buildingBox.getChildren().add(trainingStatus);
        break;

      case "Broken" :
        // provide option to rebuild the building at a cost
        TextField brokenStatus = new TextField ("Building has been destroyed!");
        brokenStatus.setEditable(false);
        buildingBox.getChildren().add(brokenStatus);
        Button repairButton = new Button("Repair Building");
        buildingBox.getChildren().add(repairButton);
        repairButton.setOnAction(new EventHandler<ActionEvent>(){
          @Override
          public void handle(ActionEvent arg0) {
            printMessageToTerminal(building.repair());
            treasury.setText(Integer.toString(province.getFaction().getTreasury()));
            int indexOf = vBox.getChildren().indexOf(buildingBox);
            vBox.getChildren().remove(buildingBox);
            displayBuildingOptions(building, vBox, indexOf);
            return;
          }
        });
        break;
    }
    if (index != 0) {
      vBox.getChildren().add(index, buildingBox);
    } else {
      vBox.getChildren().add(buildingBox);
    }
    
  }

  private HBox loadAddBuildingButton(Province province){
    // Create a new HBox for the options to add buildings
    HBox addBuildingBox = new HBox();

    // Create a combo box for all the possible buildings that can be built
    ComboBox<String> buildingTypes = new ComboBox<String>();
    buildingTypes.setVisibleRowCount(5);
    String buildingConfigurationContent = null;
    try {
      buildingConfigurationContent = Files
          .readString(Paths.get("src/unsw/gloriaromanus/configFiles/building_configuration.json"));
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    JSONObject buildingConfiguration = new JSONObject(buildingConfigurationContent);
    for (String buildingType : buildingConfiguration.keySet()) {
      JSONObject buildingJSON = buildingConfiguration.getJSONObject(buildingType).getJSONArray("level").getJSONObject(0);
      // Check if the building is not currently present in the current province and the faction has adequate money
      if (!province.buildingPresent(buildingType) && humanFaction.getTreasury() >= buildingJSON.getInt("cost")) {
        buildingTypes.getItems().add(buildingType);
      }
    }
    if (buildingTypes.getItems().size() <= 0) {
      return null;
    }
    addBuildingBox.getChildren().add(buildingTypes);

    // Create a button to add the selected building type
    Button addBuildingButton = new Button("Add building");
    addBuildingBox.getChildren().add(addBuildingButton);
    addBuildingButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        String buildingType = buildingTypes.getValue();
        if (buildingType == null) {
          printMessageToTerminal("Please select a building to add!");
          return;
        }
        try {
          printMessageToTerminal(province.addBuilding(buildingType));
        } catch (IOException e) {
          e.printStackTrace();
        }
        resetView();
        return;
      }
    });
    return addBuildingBox;
  }

  private Button addUpgradeRoadButton(Province province) {
    if (province.getRoadLevel() < 3 && humanFaction.getTreasury() >= province.getCostToUpgrade()) {
      Button upgradeRoadsButton = new Button ("Upgrade Roads");
      upgradeRoadsButton.setOnAction(new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent arg0) {
          printMessageToTerminal(province.upgradeRoads());
          resetView();
          return;
        }
      });
      return upgradeRoadsButton;
    } else {
      return null;
    }
  }
  
  private void displayUnitOptions(Unit unit, VBox vBox) {
    HBox unitHBox = new HBox();
    unitHBox.setSpacing(10);
    CheckBox unitCheckBox = new CheckBox(unit.getType());
    unitHBox.getChildren().add(unitCheckBox);
    unitCheckBox.setOnAction(new EventHandler<ActionEvent>(){
      @Override
      public void handle(ActionEvent arg0) {
        if (!unit.isSelected()){
          unit.setSelected(true);
        } else {
          unit.setSelected(true);
        }
      }  
    });
    ComboBox<Integer> numUnits = new ComboBox<Integer>();
    numUnits.setVisibleRowCount(5);
    for (int i = 1; i <= unit.getNumTroops(); i++) {
      numUnits.getItems().add(i);
    }
    unitHBox.getChildren().add(numUnits);
    numUnits.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent arg0) {
        unit.setNumSelected(numUnits.getValue());
      }
    });
    vBox.getChildren().add(unitHBox);
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

  }
}
