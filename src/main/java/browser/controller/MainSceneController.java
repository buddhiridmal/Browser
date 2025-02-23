package browser.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class MainSceneController {
    public WebView wbDisplay;
    public TextField txtAddress;
    public Button btnLoad;
    public AnchorPane root;
    String content;
    String protocol = null;
    String host = null;
    int port = 0;
    int pEndIndex=0;
    int portStartIndex=0;
    int portEndIndex=0;
    boolean valid=true;
    String path =null;


    public void initialize() throws IOException {
        wbDisplay.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) return;
            ((Stage)(root.getScene().getWindow())).setTitle("Browser - " + wbDisplay.getEngine().getTitle());
            txtAddress.setText(wbDisplay.getEngine().getLocation());
        });
        txtAddress.setText("http://google.com");
        loadWebPage(txtAddress.getText());
        txtAddress.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) Platform.runLater(txtAddress::selectAll);
        });
    }




    public void txtAddressOnAction(ActionEvent actionEvent) throws IOException {
        String url = txtAddress.getText();
        if (url.isBlank()) return;
        loadWebPage(txtAddress.getText());
        txtAddress.selectAll();

    }

    private void loadWebPage(String url) throws IOException {
        int i = 0;
        String protocol = null;
        String host = null;
        int port = -1;
        String path = "/";

        try {
            // Identify the protocol
            if ((i = url.indexOf("://")) != -1) {
                protocol = url.substring(0, i);
            }

            // Identify the host and port
            int j = url.indexOf("/", protocol == null ? i = 0 : (i = i + 3));
            host = (j != -1 ? url.substring(i, j) : url.substring(i));

            if (protocol == null) protocol = "http";

            // Separate the host and port
            if ((i = host.indexOf(":")) != -1) {
                port = Integer.parseInt(host.substring(i + 1));
                host = host.substring(0, i);
            } else {
                port = switch (protocol) {
                    case "http" -> 80;
                    case "https" -> 443;
                    default -> -1;
                };
            }

            // Identify the path + query string + fragment
            if (j != -1 && j != url.length()) path = url.substring(j);

            if (host.isBlank() || port == -1) throw new RuntimeException("Invalid URL");
            if (!(protocol.equals("http") || protocol.equals("https"))) throw new RuntimeException("Invalid protocol");

            Socket socket;
             socket = new Socket(host, port);

            OutputStream os = socket.getOutputStream();

            BufferedOutputStream bos = new BufferedOutputStream(os);

        String request = """
                GET %S HTTP/1.1
                Host: %s
                User-Agent: Browser/1
                Connection: close
                Accept: text/html
                
                """.formatted(path, host);


        System.out.println("*************************************************************************************");
        System.out.println(request);
        System.out.println("*************************************************************************************");

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
                String sts = statusLine.split(" ")[0];
                System.out.println("---------------------777777777-----------------------");
                System.out.println("statusCode : " + statusCode);

                boolean redirection = statusCode >= 300 && statusCode < 400;
                String line;
                while ((line = bsr.readLine()) != null && !line.isBlank()) {
                    String header = line.split(":")[0].strip();
                    String value = line.substring(line.indexOf(":") + 1);
                    System.out.println(".................................................................");
                    System.out.println(header + " : " + value);
                    /*if (redirection) {
                        if (!header.equalsIgnoreCase("Location")) continue;
                        System.out.println("Redirection" + value);
                        Platform.runLater(() -> txtAddress.setText(value));
                        loadWebPage(value);
                        return;
                    }*/

                    if (redirection) {
                        if (header.equalsIgnoreCase("Location")) {
                            String redirectUrl = value.strip();

                            // Ensure the redirect URL has the correct protocol
                            if (!redirectUrl.startsWith("http")) {
                                redirectUrl = protocol + "://" + host + (redirectUrl.startsWith("/") ? "" : "/") + redirectUrl;
                            }

                            System.out.println("Redirection detected: " + redirectUrl);

                            String finalRedirectUrl = redirectUrl; // Required for lambda
                            Platform.runLater(() -> {
                                txtAddress.setText(finalRedirectUrl);
                                try {
                                    loadWebPage(finalRedirectUrl);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            return; // Exit the thread to avoid further processing
                        }
                    }

                    else {
                        if (!header.equalsIgnoreCase("content-type")) continue;
                        contentType = value;
                    }
                }
                System.out.println("Content Type --------------->: " + contentType);
                content = "";
                while ((line = bsr.readLine()) != null) {
                    content += (line + "\n");
                }
                System.out.println("Content" + "\n"+ content);
            }catch (Exception e){
                e.printStackTrace();
            }



        }).start();

    }


    public void btnLoadOnAction(ActionEvent actionEvent) {

        wbDisplay.getEngine().loadContent(content, "text/html");
    }
}
