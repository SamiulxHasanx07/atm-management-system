package com.example.atmmanagementsystem;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AtmController {
    @FXML
    private TextField screenInput;

    @FXML
    private void onNum(javafx.event.ActionEvent ev) {
        String val = ((javafx.scene.control.Button) ev.getSource()).getText();
        if (screenInput != null) screenInput.appendText(val);
    }

    @FXML
    private void onClear() {
        if (screenInput != null) screenInput.clear();
    }

    @FXML
    private void onOk() {
        if (screenInput != null) {
            // For demo, just echo the input into the field and clear after
            System.out.println("ATM OK pressed. Input=" + screenInput.getText());
            screenInput.clear();
        }
    }
}
