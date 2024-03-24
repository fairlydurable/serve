package server;

import java.net.URLDecoder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

class ContentUpdateHandler implements HttpHandler {
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String response = "uninitialized";
        
        if (query != null && query.contains("content=")) {
            try {
                String encodedString = query.split("content=")[1];
                String decodedString = URLDecoder.decode(encodedString, "UTF-8");
                response="OK";
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, -1);
            } catch (UnsupportedEncodingException e) {
                response = "Unsupported encoding: " + e.getMessage();
                exchange.sendResponseHeaders(400, response.getBytes().length);
            } catch (NumberFormatException e) {
                response = "Unable to set new content: " + e.getMessage();
                exchange.sendResponseHeaders(400, response.getBytes().length);
            }
        } else {
            response = "Bad Request";
            exchange.sendResponseHeaders(400, response.getBytes().length);
        }
        
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
