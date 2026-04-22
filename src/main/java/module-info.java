module com.example.atmmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires com.google.gson;
    requires java.desktop;

    opens com.example.atmmanagementsystem to javafx.fxml;
    opens com.example.atmmanagementsystem.api.dto to com.google.gson;
    opens com.example.atmmanagementsystem.model to com.google.gson;

    exports com.example.atmmanagementsystem;
    exports com.example.atmmanagementsystem.api;
    exports com.example.atmmanagementsystem.api.dto;
    exports com.example.atmmanagementsystem.model;
    exports com.example.atmmanagementsystem.service;
}