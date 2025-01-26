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
