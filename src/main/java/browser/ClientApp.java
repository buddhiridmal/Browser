package browser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientApp  {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("google.lk", 80);
        System.out.println("connected to google with the socket "+ socket.getRemoteSocketAddress());

        new Thread(()->{
            try {
                InputStream is = socket.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

    }}
