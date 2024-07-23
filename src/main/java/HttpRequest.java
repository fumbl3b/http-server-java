import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String uri;
    private String httpVersion;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public HttpRequest(BufferedReader in) throws IOException {
        parseRequest(in);
    }

    private void parseRequest(BufferedReader in) throws IOException {
        // Take the line and parse it
        String requestLine = in.readLine();
        if (requestLine == null) {
            throw new IllegalArgumentException("Invalid HTTP request");
        }

        String[] parts = requestLine.split(" ", 3); 
        if (parts.length == 3) {
            this.method = parts[0];
            this.uri = parts[1];
            this.httpVersion = parts[2];
        } else {
            throw new IllegalArgumentException("Invalid HTTP request" + " | Missing parts");
        }

        // Parse the headers
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            String[] headerDuo = line.split(": ", 2);
            if (headerDuo.length == 2) {
                headers.put(headerDuo[0], headerDuo[1]);
            }
        }

        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars);
            this.body = new String(bodyChars);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
        "method='" + method + '\'' +
        ", uri='" + uri + '\'' +
        ", httpVersion='" + httpVersion + '\'' +
        ", headers=" + headers +
        ", body='" + body + '\'' +
        '}';
    }
}