import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) {
        int port = Constant.PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server started on port " + port + " ...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("accepted new connection");
                new Thread(new ClientHandler(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}
