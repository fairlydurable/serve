package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

import java.net.InetSocketAddress;

public class Server {
    static String localhost = "127.0.0.1";
    static String dropletHost = "146.190.45.234";
    
    public static void main(String[] args) throws IOException {
        int    port = 8080;
        String host = localhost;
        
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", new StaticFileHandler("./"));
        server.setExecutor(null);
        
        System.out.printf("Server %s:%d\n", host, port);
        server.start();
    }
}
