package com.example.atmmanagementsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BanglaBankApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BanglaBankApplication.class.getResource("bangla-bank-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Bangla Bank - Management System");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(550);
        stage.setMinHeight(600);
    }
}
