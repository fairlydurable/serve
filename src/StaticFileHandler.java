package server;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import java.nio.file.Files;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class StaticFileHandler implements HttpHandler {
    private final String root;
    
    public StaticFileHandler(String rootDir) {
        this.root = rootDir;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestedFile = exchange.getRequestURI().getPath();
        
        // If the request is for the root directory, serve 'index.html'
        if (requestedFile.equals("/")) {
            requestedFile = root + "html/index.html";
        }

        // Security check to prevent access to restricted files
        if (requestedFile.contains("..") || requestedFile.contains("/.") || requestedFile.contains(".\\")) {
            sendResponse(exchange, 403, "Forbidden".getBytes());
            return;
        }
        
        File file = new File(root + requestedFile).getCanonicalFile();
        System.out.println("Looking for " + file);

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
            final byte[] buffer = new byte[0x10000];
            int count;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, byte[] responseBytes) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
