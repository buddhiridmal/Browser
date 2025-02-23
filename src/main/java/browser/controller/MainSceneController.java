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
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

            // Establish the connection
            Socket socket = new Socket(host, port);
            String baseUrl = protocol + "://" + host + ":" + port + "/";

            // Read the server response
            new Thread(() -> {
                try {
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader bsr = new BufferedReader(isr);

                    //Read the status line
                    String statusLine = bsr.readLine();
                    int statusCode = Integer.parseInt(statusLine.split(" ")[1]);
                    boolean redirection = statusCode >= 300 && statusCode < 400;

                    // Read the request headers
                    String line;
                    String contentType = null;
                    boolean chunked = false;

                    while ((line = bsr.readLine()) != null && !line.isBlank()) {
                        String header = line.split(":")[0].strip();
                        String value = line.substring(line.indexOf(":") + 1).strip();
                        if (redirection) {
                            if (!header.equalsIgnoreCase("Location")) continue;
                            System.out.println("Redirection: " + value);
                            Platform.runLater(() -> txtAddress.setText(value));
                            loadWebPage(value);
                            return;
                        } else {
                            if (header.equalsIgnoreCase("Content-Type")) {
                                contentType = value;
                            } else if (header.equalsIgnoreCase("Transfer-Encoding")) {
                                chunked = value.equalsIgnoreCase("chunked");
                            }
                        }
                    }



                    if (contentType == null || !contentType.contains("text/html")) {
                        displayError("Sorry, content type not supported");
                    } else {
                        if (chunked) bsr.readLine();    // Skip the chunk size
                        StringBuilder sb = new StringBuilder();
                        while ((line = bsr.readLine()) != null) {
                            sb.append(line);
                        }
                        if (chunked) sb.deleteCharAt(sb.length() - 1); // Delete the chunk boundary

                        // Fix the relative url issue with the base url
                        if (!Pattern.compile("<head>.*<base .*</head>").matcher(sb).find()) {
                            Matcher headMatcher = Pattern.compile("<head>").matcher(sb);
                            if (headMatcher.find()) {
                                sb.insert(headMatcher.end(), "<base href='%s'>".formatted(baseUrl));
                            }
                        }

                        // Set the title
                        Matcher titleMatcher = Pattern.compile("<title>(.+)</title>", Pattern.CASE_INSENSITIVE).matcher(sb);
                        if (titleMatcher.find()) {
                            Platform.runLater(() -> {
                                ((Stage) (root.getScene().getWindow())).setTitle("Browser - " + titleMatcher.group(1));
                            });
                        }


                        Platform.runLater(() -> wbDisplay.getEngine().loadContent(sb.toString()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    displayError("Connection Failed");
                }
            }).start();

            // Send the client request
            String httpRequest = """
                    GET %s HTTP/1.1
                    Host: %s
                    User-Agent: Mozilla/5.0
                    Connection: close
                    Accept: text/html
                    
                    """.formatted(path, host);
            socket.getOutputStream().write(httpRequest.getBytes());
            socket.getOutputStream().flush();
        } catch (RuntimeException | UnknownHostException e) {
            displayError("400 Bad Request: Invalid URL");
        } catch (IOException e) {
            displayError("Connection Error");
        }
    }





    private void displayError(String message) {
        Platform.runLater(() -> {
            wbDisplay.getEngine().loadContent("""
                    <!DOCTYPE html>
                    <html>
                    <body>
                    <h1 style="text-align: center;">%s</h1>
                    </body>
                    </html>
                    """.formatted(message));
        });
    }
}
