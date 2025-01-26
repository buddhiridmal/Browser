package browser;

import java.io.*;
import java.net.Socket;

public class ClientApp  {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("google.lk", 80);
        System.out.println("connected to google with the socket "+ socket.getRemoteSocketAddress());

        new Thread(()->{
            try {
                InputStream is = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                while (true){
                    byte[] buffer = new byte[1024];
                    int read = bis.read(buffer);
                    if (read == -1) break;
                    System.out.print(new String(buffer, 0, read));
                }
                System.out.println("server stopped responding");


                String url1="google.lk";
                String url2="www.google.lk/search?q=ijse";
                String url3="jdbc:mysql://127.0.0.1:3306/dep13";
                String url4="lcc://test";
                String url5="lcc://test:7580/abc";

                String url=url5;
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


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

        OutputStream os = socket.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(os);
        String httpRequest = """
                GET / HTTP/1.1
                Host: gogle.lk
                User-Agent: browser/1.0.0
                Connection: close
                
                """;
        bos.write(httpRequest.getBytes());
        bos.flush();



    }}
