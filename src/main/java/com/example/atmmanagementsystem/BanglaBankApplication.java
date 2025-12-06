package com.example.atmmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BanglaBankApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BanglaBankApplication.class.getResource("atm-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Bangla Bank - Management System");
        stage.setScene(scene);
        // set minimum size
        stage.setMinWidth(550);
        stage.setMinHeight(600);

        // maximize and start in fullscreen
        stage.setMaximized(true);
        stage.setFullScreen(true);

        stage.show();
    }
}
