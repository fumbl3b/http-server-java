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
        HttpRequest request = null;
        try (BufferedReader in = 
                new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = 
                clientSocket.getOutputStream();) {
            // Parse the request line
            try {
                request = new HttpRequest(in);
            } catch (IllegalArgumentException | IOException e) {
                sendResponse(out, 400, "Bad Request");
            }
            // Logging
            System.out.println("Received: \n" + request);
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                sendResponse(out, 405, "Method Not Allowed");
            }
            
            String requestUri = request.getUri();
            if (requestUri.equals("/")) sendResponse(out, 200, "");
            else if (requestUri.startsWith("/echo/")) sendResponse(out, 200, requestUri.substring(6));
            else if (requestUri.equals("/user-agent") ) {
                String userAgent = request.getHeaders().get("User-Agent");
                sendResponse(out, 200, userAgent);
            }
            else out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
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
