package browser.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;

import java.io.*;
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

       // url=url5;
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
            System.out.println("-----------------------------------------------------------------");
        }else System.out.println("Invalid URL");

        Socket socket = new Socket(host, port);
        System.out.println("Connected to " +socket.getRemoteSocketAddress());

        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);

        String request = """
                GET %S HTTP/1.1
                Host: %s
                User-Agent: Browser/1
                Connection: close
                Accept: text/html
                
                """.formatted(path, host);

        bos.write(request.getBytes());
        bos.flush();

        new Thread(()->{
            try {
                String contentType = null;

                /*InputStream is = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                while (true){
                    byte[] buffer = new byte[1024];
                    int read = bis.read(buffer);
                    if (read == -1) break;
                    System.out.print(new String(buffer, 0, read));
                }
                System.out.println("server stopped responding");*/


              InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader bsr = new BufferedReader(isr);

                // Read the status line
                String statusLine = bsr.readLine();
                int statusCode = Integer.parseInt(statusLine.split(" ")[1]);
                System.out.println("--------------------------------------------");
                System.out.println("statusCode : " + statusCode);

                boolean redirection = statusCode >= 300 && statusCode < 400;
                String line;
                while ((line = bsr.readLine()) != null && !line.isBlank()) {
                    String header = line.split(":")[0].strip();
                    String value = line.substring(line.indexOf(":") + 1);
                    System.out.println("***********************************");
                    System.out.println(header);
                    System.out.println(value);

                    if (redirection) {
                        if (!header.equalsIgnoreCase("Location")) continue;
                        System.out.println("Redirection" + value);
                        Platform.runLater(() -> txtAddress.setText(value));
                        loadWebPage(value);
                        return;
                    } else {
                        if (!header.equalsIgnoreCase("content-type")) continue;
                        contentType = value;
                    }

                }
                System.out.println("Content Type --------------->: " + contentType);




            }catch (Exception e){
            }

        }).start();



    }


}
