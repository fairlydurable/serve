package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;

public class Server {
    private static boolean isValidIPAddress(String ipAddress) {
        // Regular expression to match IPv4 address format
        String ipRegex = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        return Pattern.matches(ipRegex, ipAddress);
    }
    
    private static boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            // Exclude well-known ports below 1024. Consider ports over 49151
            // as generally acceptable for private or dynamic use.
            return port >= 1024 && port <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static String localhost = "127.0.0.1";
    static String dropletHost = "146.190.45.234";
    
    public static void main(String[] args) throws IOException {
        // Default values
        int port = 8080;
        String host = localhost;

        if (args.length > 0 && isValidIPAddress(args[0])) { host = args[0]; }
        if (args.length > 1 && isValidPort(args[1])) { port = Integer.parseInt(args[1]); }

        // Create and start the server
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null); // creates a default executor

        // Output the server details
        System.out.printf("Server starting on %s:%d\n", host, port);
        server.start();
    }
}
