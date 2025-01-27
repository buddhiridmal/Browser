package browser.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainSceneController {
    public WebView wbDisplay;
    public TextField txtAddress;

    public void initialize() throws IOException {
        txtAddress.setText("http://www.google.com");
        loadWebPage(txtAddress.getText());
        txtAddress.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> txtAddress.selectAll());
            }
        });


    }




    public void txtAddressOnAction(ActionEvent actionEvent) throws IOException {
        String url = txtAddress.getText();
        if (url.isBlank()) return;

        loadWebPage(url);
    }

    private void loadWebPage(String url) throws IOException {
        String url1="google.lk";
        String url2="www.google.lk/search?q=ijse";
        String url3="jdbc:mysql://127.0.0.1:3306/dep13";
        String url4="lcc://test";
        String url5="lcc://test:7580/abc";

        url=url5;
        String noProtocol;

        String protocol = null;
        String host = null;
        int port = 0;
        int pEndIndex=0;
        int portStartIndex=0;
        int portEndIndex=0;
        boolean valid=true;
        String path =null;

        if (url.contains("://")){
            pEndIndex=url.indexOf("://");
            protocol=url.substring(0,pEndIndex);
            pEndIndex+=3;
        }else {
            //pEndIndex=-3;
            protocol="http";
        }
        noProtocol=url.substring(pEndIndex);
        //System.out.println("After removing the protocol -> "+noProtocol);

        if (protocol.equals("http")){
            port=80;
            if (noProtocol.contains("/")){
                host=noProtocol.substring(0,noProtocol.indexOf("/"));
                path=noProtocol.substring(noProtocol.indexOf("/")+1);
            }else {host=noProtocol;
                path="/";
            }
        } else if (protocol.equals("https")) {
            port=443;
            if (noProtocol.contains("/")){
                host=noProtocol.substring(0,noProtocol.indexOf("/"));
                path=noProtocol.substring(noProtocol.indexOf("/")+1);
            }else {host=noProtocol;
                path="/";
            }

        }else if (noProtocol.contains(":")){
            portStartIndex=noProtocol.indexOf(":");
            portEndIndex=noProtocol.indexOf("/");
            port=Integer.parseInt(noProtocol.substring(portStartIndex+1,portEndIndex));
            host=noProtocol.substring(0,noProtocol.indexOf(":"));
            if (noProtocol.contains("/")) {
                path = noProtocol.substring(noProtocol.indexOf("/") + 1);
            }else path="/";
        }else {
            valid=false;
        }

        //System.out.println(portStartIndex);
        //System.out.println(portStartIndex);

        if (valid){
            System.out.println("protocol  ->  "+protocol);
            //System.out.println("After removing the protocol -> "+noProtocol);
            System.out.println("host -> "+host);
            System.out.println("port -> "+port);
            System.out.println("path -> "+path);
        }else System.out.println("Invalid URL");

        Socket socket = new Socket(host, port);
        System.out.println("Connected to " +socket.getRemoteSocketAddress());

        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);


    }


}
