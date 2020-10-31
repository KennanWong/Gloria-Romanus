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

import javafx.*;
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

    @FXML
    public void clickedOfflineGameButton(ActionEvent event) throws IOException {
        if (faction_1 == null && faction_2 == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            GloriaRomanusController controller = loader.getController();
            controller.setTest("noob");
            Scene game = new Scene(root);
            Stage stageTheEventSourceNodeBelongs = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stageTheEventSourceNodeBelongs.setScene(game);
        } else {
            FactionData data = new FactionData();
            data.setFaction1((String) faction_1.getText());
            data.setFaction2((String) faction_2.getText());

            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Parent root = loader.load();
            GloriaRomanusController controller = loader.getController();
            stage.setUserData(data);
            // Step 6
            Scene scene = new Scene(root);
            stage.setScene(scene);
            // Step 7
            stage.show();
        }
        
    }


    private void printMessageToTerminal(String message) {
        output_terminal.appendText(message + "\n");
    }
}
