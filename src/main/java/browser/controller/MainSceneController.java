package browser.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

public class MainSceneController {
    public WebView wbDisplay;
    public TextField txtAddress;

    public void initialize() {
        txtAddress.setText("http://google.lk");


    }




    public void txtAddressOnAction(ActionEvent actionEvent) {
    }




}
