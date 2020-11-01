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
    private TextField faction_1;

    @FXML
    private TextField faction_2;
    @FXML
    private TextArea output_terminal;

    private List<String> factionNames;

    @FXML
    private void initialize() throws JsonParseException, JsonMappingException, IOException {
        printMessageToTerminal("Welcome to Gloria Romanus!\n");
        factionNames = new ArrayList<>();
        String intialOwnershipContent = Files.readString(Paths.get("src/unsw/gloriaromanus/initial_province_ownership.json"));
        JSONObject ownership = new JSONObject(intialOwnershipContent);
        for (String faction : ownership.keySet()) {
            factionNames.add(faction);
        }
        printMessageToTerminal("Here are the possible factions you are able to choose from: \n");
        JSONObject factionAssignmentJSON = new JSONObject();
        int playerCounter = 1;
        for (String factionName : factionNames) {
            printMessageToTerminal(factionName + "   ");
            factionAssignmentJSON.put(factionName, playerCounter);
            playerCounter++;
        }

        String factionAssignmentContent = factionAssignmentJSON.toString();
        Path fileName = Path.of("src/unsw/gloriaromanus/faction_assignment.json");
        Files.writeString(fileName, factionAssignmentContent);


        printMessageToTerminal("\n");
        

    }

    @FXML
    public void clickedOfflineGameButton(ActionEvent event) throws IOException {
        if (faction_1.getText().length() < 1  && faction_2.getText().length() < 1) {
            // Start game with default province assignment
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            GloriaRomanusController controller = loader.getController();
            Scene game = new Scene(root);
            Stage stageTheEventSourceNodeBelongs = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stageTheEventSourceNodeBelongs.setScene(game);
        } else {
            if (factionNames.contains(faction_1.getText()) && factionNames.contains(faction_2.getText()) 
                && !faction_1.getText().equals(faction_2.getText())) {
                // Set the faction assignment
                JSONObject factionAssignmentJSON = new JSONObject();
                factionAssignmentJSON.put(faction_1.getText(), 1);
                factionAssignmentJSON.put(faction_2.getText(), 2);
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
            } else {
                printMessageToTerminal("Incorrect selection of factions!\n");
            }
        }

        
    }


    private void printMessageToTerminal(String message) {
        output_terminal.appendText(message);
    }
}
