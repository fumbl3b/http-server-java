import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Stack;

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
                sendResponse(out, 400, "", "Bad Request");
            }
            // Logging
            System.out.println("Received: \n" + request);
            String requestUri = request.getUri();

            if ("GET".equalsIgnoreCase(request.getMethod())) {
                if (requestUri.equals("/")) sendResponse(out, 200, "", null);
                else if (requestUri.startsWith("/echo/")) sendResponse(out, 200, requestUri.substring(6), null);
                else if (requestUri.equals("/user-agent") ) {
                    String userAgent = request.getHeaders().get("User-Agent");
                    sendResponse(out, 200, userAgent);
                } else if (requestUri.startsWith("/files/")) {
                    handleFileRequest(out, requestUri);
                }
                else out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            } else if ("POST".equalsIgnoreCase(request.getMethod())) {
                if (requestUri.startsWith("/files/")) {
                    handleFileCreation(out, requestUri, request);
                } else {
                    sendResponse(out, 404, "", "Not Found");
                }
            } else {
                sendResponse(out, 405, "", "Method Not Allowed");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleFileRequest(OutputStream out, String requestUri) throws IOException {
        String fileName = requestUri.substring("/files/".length());
        File file = findFile(new File("/tmp/"), fileName);

        if (file == null || !file.exists() || file.isDirectory()) {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileContent = new byte[(int) file.length()];
            fis.read(fileContent);

            String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/octet-stream\r\n" +
                            "Content-Length: " + file.length() + "\r\n" +
                            "\r\n";
            out.write(header.getBytes());
            out.write(fileContent);
        }
    }

    private File findFile(File dir, String filename) {
        Stack<File> stack = new Stack<>();
        stack.push(dir);

        while (!stack.isEmpty()) {
            File currentDir = stack.pop();
            File[] files = currentDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        stack.push(file);
                    } else if (file.getName().equals(filename)) {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    private void handleFileCreation(OutputStream out, String requestUri, HttpRequest request) throws IOException {
        String filename = requestUri.substring("/files/".length());
        File file = new File("/tmp/" + filename);

        if (file.exists()) {
            sendResponse(out, 409, "", "File Already Exists");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(request.getBody().getBytes());
        }

        sendResponse(out, 201, "File Created", "Created");
    }

    private void sendResponse(OutputStream out, int statusCode, String responseText, String reason) throws IOException {
        if (reason == null) reason = "OK";
        String response = "HTTP/1.1 " + statusCode + " " + reason + "\r\n" +
                          "Content-Type: text/plain\r\n" +
                          "Content-Length: " + responseText.length() + "\r\n" +
                          "\r\n" +
                          responseText;
        out.write(response.getBytes());
        out.flush();
    }
}
