package com.example.atmmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.awt.Desktop;
import java.net.URI;

public class InfoController {

    @FXML
    protected void onBack(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("app.fxml"));
            javafx.scene.Parent root = loader.load();
            ((Node) event.getSource()).getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void openProfileLink() {
        openUrl("https://github.com/SamiulxHasanx07");
    }

    @FXML
    protected void openFrontendRepoLink() {
        openUrl("https://github.com/SamiulxHasanx07/atm-management-system");
    }

    @FXML
    protected void openBackendRepoLink() {
        openUrl("https://github.com/SamiulxHasanx07/atm-management-system-backend");
    }

    @FXML
    protected void openLiveSystemLink() {
        openUrl("https://atm-management-system-backend.vercel.app/");
    }

    private void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
