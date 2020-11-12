package unsw.gloriaromanus;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.event.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.json.JSONObject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import unsw.gloriaromanus.*;

public class MenuController {
    @FXML
    private TextField player_1;
    @FXML
    private Button faction_1_name;
    @FXML
    private Button faction_2_name;
    @FXML
    private TextField player_2;
    @FXML
    private TextArea output_terminal;

    private List<String> factionNames;

    @FXML
    private void initialize() throws JsonParseException, JsonMappingException, IOException {
        output_terminal.setStyle("-fx-text-alignment: center");
        printMessageToTerminal("Welcome to Gloria Romanus!\n");
        factionNames = new ArrayList<>();
        String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
        for (String faction : ownership.keySet()) {
            factionNames.add(faction);
        }

        faction_1_name.setText(factionNames.get(0));
        faction_2_name.setText(factionNames.get(1));
        printMessageToTerminal("Here are the possible factions you are able to choose from: \n");
        JSONObject factionAssignmentJSON = new JSONObject();
        int playerCounter = 1;
        for (String factionName : factionNames) {
            printMessageToTerminal(factionName + "   ");
            factionAssignmentJSON.put(factionName, playerCounter);
            playerCounter++;
        }
        printMessageToTerminal("\nPlayer 1 please choose a faction \n");


        printMessageToTerminal("\n");
        

    }

    @FXML
    public void clickedOfflineGameButton(ActionEvent event) throws IOException {
        if (player_1.getText().length() < 1  && player_2.getText().length() < 1) {
            // Start game with default province assignment
            printMessageToTerminal("Please select a faction before starting the game.\n");
            return;
        } 
        // Set the faction assignment
        JSONObject factionAssignmentJSON = new JSONObject();
        factionAssignmentJSON.put(player_1.getText(), 1);
        factionAssignmentJSON.put(player_2.getText(), 2);
        String factionAssignmentContent = factionAssignmentJSON.toString();
        Path fileName = Path.of("src/unsw/gloriaromanus/faction_assignment.json");
        Files.writeString(fileName, factionAssignmentContent);
        
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();
        GloriaRomanusController controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
    }

    @FXML
    public void clickedFaction1(ActionEvent event) {
        player_1.setText(faction_1_name.getText());
        player_2.setText(faction_2_name.getText());
    }

    @FXML
    public void clickedFaction2(ActionEvent event) {
        player_1.setText(faction_2_name.getText());
        player_2.setText(faction_1_name.getText());
    }


    private void printMessageToTerminal(String message) {
        output_terminal.appendText(message);
    }
}
