package server;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class StaticFileHandler implements HttpHandler {
    private HttpExchange exchange;
    private String root;
    
    public StaticFileHandler() {
        this.root = "src/";
    }
    
    public String getQueryParamValue(HttpExchange exchange, String paramName) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query != null && !query.isEmpty()) {
                String[] parameters = query.split("&");
                for (String parameter : parameters) {
                    if (parameter.startsWith(paramName + "=")) {
                        String value = parameter.substring((paramName + "=").length());
                        String decodedValue = URLDecoder.decode(value, "UTF-8");
                        if (!decodedValue.isEmpty()) { return decodedValue; }
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return null;
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
    
    public void sendFile(HttpExchange exchange, File file) throws IOException {
        if (!file.isFile()) {
            // Object does not exist or is not a file: reject with 404 error.
            String response = "404 (Not Found)\n";
            sendResponse(exchange, 404, response.getBytes());
            return;
        }
        
        // Object exists and is a file: accept with response code 200.
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, file.length());
        
        try (OutputStream os = exchange.getResponseBody(); FileInputStream fs = new FileInputStream(file)) {
            final byte[] buffer = new byte[0x10000]; // 64 KB buffer
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
        }
    }
    
    private void handleFile(HttpExchange exchange) throws IOException {
        String requestedFile = exchange.getRequestURI().getPath();
        
        // Security check to prevent access to restricted files
        if (requestedFile.contains("..") || requestedFile.contains("/.") || requestedFile.contains(".\\")) {
            sendResponse(exchange, 403, "Forbidden".getBytes());
            return;
        }
        
        // Normalize the requested file path to prevent directory traversal attacks
        try {
            requestedFile = new File(requestedFile).getCanonicalPath();
        } catch (IOException e) {
            sendResponse(exchange, 400, "Bad Request".getBytes());
            return;
        }
        
        // Serve 'index.html' for root or specified directory displays
        List<String> indexPages = Arrays.asList("/", "/display", "/mantra");
        if (indexPages.contains(requestedFile) || new File(root + requestedFile).isDirectory()) {
            requestedFile = "html" + requestedFile + "/index.html";
        }
        
        File file = new File(root, requestedFile);
//        System.out.println("Requested: " + requestedFile);
//        System.out.println("Retrieving: " + file.getAbsolutePath());
        try {
            sendFile(exchange, file);
        } catch (IOException e) {
            throw e;
        }
    }
    
    private void handleAPIRequest(HttpExchange exchange) throws IOException {
        String endpoint = exchange.getRequestURI().getPath();
        List<String> endpoints = Arrays.asList("/display", "/mantra");
        if (!endpoints.contains(endpoint)) {
            System.out.println("Unhandled endpoint: " + endpoint);
            return;
        }
        
        // Common endpoints
        String update = null;
        String newContent = null;
        File file;

        switch(endpoint) {
            case "/display":
                try {
                    update = getQueryParamValue(exchange, "update");
                } catch (IOException e) {
                    throw e;
                }
                newContent = "{ \"content\":\"" + update + "\" }";
                file = new File(root, "/html/display/content.json");
                saveContentToFile(file, newContent);
                handleFile(exchange);
                break;
            case "/mantra":
                try {
                    update = getQueryParamValue(exchange, "update");
                } catch (IOException e) {
                    throw e;
                }
                newContent = "{ \"content\":\"" + update + "\" }";
                file = new File(root, "/html/mantra/content.json");
                saveContentToFile(file, newContent);
                handleFile(exchange);
                break;
            default:
                break;
        }
    }

    @Override
    public void handle(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        boolean isAPIRequest = query != null && !query.isEmpty();
        
        try {
            if (isAPIRequest) {
                handleAPIRequest(exchange);
            } else {
                handleFile(exchange);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
