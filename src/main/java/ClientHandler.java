import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        try (BufferedReader in = 
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = 
                clientSocket.getOutputStream();) {
        
            // Parse the request line
            // TODO: add POST, FETCH, 
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) {
                sendResponse(out, 405, "Method Not Allowed");
                return;
            }

            String[] requestParts = requestLine.split(" ", 0);

            //TODO: remove this
            for (String s : requestParts) {
                System.out.println(s);
            }

            if (requestParts[1].equals("/")) {
                sendResponse(out, 200, "");
            } else if (requestParts[1].startsWith("/echo/")) {
                sendResponse(out, 200, requestParts[1].substring(6));
            } else {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
            

        } catch (IOException e) {
            
        }

    }

    private void sendResponse(OutputStream out, int statusCode, String responseText) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " OK\r\n" +
                          "Content-Type: text/plain\r\n" +
                          "Content-Length: " + responseText.length() + "\r\n" +
                          "\r\n" +
                          responseText;
        out.write(response.getBytes());
        out.flush();
    }
}
