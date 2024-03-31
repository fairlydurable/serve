package server;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.*;

public class StaticFileHandler implements HttpHandler {
    private HttpExchange exchange;
    private String root;
    private Map<String, String> queryParams;
    private boolean hasQueryParams;

    public StaticFileHandler() {
        this.root = "src/html/";
    }

    public Map<String, String> parseQueryParams(HttpExchange exchange) {
        Map<String, String> queryParams = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();

        if (query != null && !query.isEmpty()) {
            String[] parameters = query.split("&");
            for (String parameter : parameters) {
                try {
                    String[] keyValuePair = parameter.split("=", 2);
                    if (keyValuePair.length == 2) {
                        String key = keyValuePair[0];
                        String value = URLDecoder.decode(keyValuePair[1], "UTF-8");
                        queryParams.put(key, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        return queryParams;
    }

    public Optional<String> getQueryParamValue(String paramName) {
        if (queryParams != null && queryParams.containsKey(paramName)) {
            return Optional.ofNullable(queryParams.get(paramName));
        } else {
            return Optional.empty();
        }
    }

    public static void saveContentToFile(File file, String content) {
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, byte[] responseBytes) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void respond(HttpExchange exchange, int statusCode, String message) {
        try {
              byte[] responseBytes = message.getBytes("UTF-8");
              sendResponse(exchange, statusCode, responseBytes);
          } catch (IOException e) {
              e.printStackTrace();
          }
    }

    public void sendFile(HttpExchange exchange, File file) {
        if (!file.isFile()) {
            respond(exchange, 404, "404 (Not Found)\n");
            return;
        }

        try {
            String mimeType = Files.probeContentType(file.toPath());
            mimeType = mimeType != null ? mimeType : "application/octet-stream";
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, file.length()); // OK status

            try (OutputStream os = exchange.getResponseBody(); FileInputStream fs = new FileInputStream(file)) {
                final byte[] buffer = new byte[0x10000]; // 64 KB buffer
                int count;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            respond(exchange, 500, "Internal Server Error");
        }
    }

    private void handleFile(HttpExchange exchange) {
        String requestedFile = exchange.getRequestURI().getPath();

        // Security check to prevent access to restricted files
        if (requestedFile.contains("..") || requestedFile.contains("/.") || requestedFile.contains(".\\")) {
            respond(exchange, 403, "Forbidden");
            return;
        }
        
        // Normalize the requested file path to prevent directory traversal attacks
        try {
            requestedFile = new File(requestedFile).getCanonicalPath();
        } catch (IOException e) {
            respond(exchange, 400, "Bad Request");
            return;
        }
        
        // Serve 'index.html' for root or specified directory displays
        List<String> indexPages = Arrays.asList("/", "/display", "/mantra");
        if (indexPages.contains(requestedFile) || new File(root + requestedFile).isDirectory()) {
            requestedFile = requestedFile + "/index.html";
        }
        
        File file = new File(root, requestedFile);
        sendFile(exchange, file);
    }
    
    private void handleAPIRequest(HttpExchange exchange) {
        String endpoint = exchange.getRequestURI().getPath();
        List<String> endpoints = Arrays.asList("/display", "/mantra");
        if (!endpoints.contains(endpoint)) {
            System.out.println("Unhandled endpoint: " + endpoint);
            return;
        }
        
        File file;
        String newContent = "";
        String update = getQueryParamValue("update").orElse("");

        switch(endpoint) {
            case "/display":
                newContent = "{ \"content\":\"" + update + "\" }";
                file = new File(root, "/display/content.json");
                saveContentToFile(file, newContent);
                handleFile(exchange);
                break;
            case "/mantra":
                newContent = "{ \"content\":\"" + update + "\" }";
                file = new File(root, "/mantra/content.json");
                saveContentToFile(file, newContent);
                handleFile(exchange);
                break;
            default:
                break;
        }
    }

    @Override
    public void handle(HttpExchange exchange) {
        queryParams = parseQueryParams(exchange);
        hasQueryParams = !queryParams.isEmpty();
        if (hasQueryParams) {
            handleAPIRequest(exchange);
        } else {
            handleFile(exchange);
        }
    }
}
