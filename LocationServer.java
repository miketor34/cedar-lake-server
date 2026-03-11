import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;

public class LocationServer {

    static ArrayList<String> locationLog = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // /ping — lets the website check if we are running
        server.createContext("/ping", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String response = "BlueJ server is running";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        // /location — receives each GPS update from the website
        server.createContext("/location", exchange -> {
            addCorsHeaders(exchange);
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Read the data sent from the browser
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes()).trim();

            // Store and print it
            locationLog.add(body);
            System.out.println("[" + locationLog.size() + "] Received: " + body);

            // Parse lat/lon/lake, check the zone, send true/false back to browser
            String response = "false";
            String[] parts = body.split(",");
            if (parts.length >= 3) {
                try {
                    double lat = Double.parseDouble(parts[1]);
                    double lon = Double.parseDouble(parts[2]);

                    // Get lake key from part 6, default to big_cedar
                    String lakeKey = parts.length >= 6 ? parts[5].trim() : "big_cedar";
                    String filename = lakeKey + "_surf_zones.csv";

                    boolean acceptable = LocationAnalyzer.getResult(lat, lon, filename);
                    response = String.valueOf(acceptable);
                    System.out.println("  Lake: " + lakeKey + " | Zone check: " + response);
                } catch (Exception e) {
                    System.out.println("  Error parsing coordinates: " + e.getMessage());
                }
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        server.start();

        System.out.println("=========================================");
        System.out.println("  BlueJ Location Server started!");
        System.out.println("  Listening on http://localhost:8080");
        System.out.println("  Open location.html in Chrome now.");
        System.out.println("=========================================");
        System.out.println("  Each GPS update will print below...");
        System.out.println("-----------------------------------------");
    }

    public static void saveToFile() {
        if (locationLog.isEmpty()) {
            System.out.println("No locations recorded yet! Start tracking first.");
            return;
        }
        try {
            FileWriter writer = new FileWriter("location_log.txt");
            writer.write("timestamp,latitude,longitude,accuracy,altitude\n");
            for (String entry : locationLog) {
                writer.write(entry + "\n");
            }
            writer.close();
            System.out.println("Saved " + locationLog.size() + " locations to location_log.txt");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public static void printAll() {
        if (locationLog.isEmpty()) {
            System.out.println("No locations received yet.");
            return;
        }
        System.out.println("=========================================");
        System.out.println("  ALL RECEIVED LOCATIONS (" + locationLog.size() + " total)");
        System.out.println("=========================================");
        for (int i = 0; i < locationLog.size(); i++) {
            String[] parts = locationLog.get(i).split(",");
            System.out.println("Stop #" + (i + 1));
            if (parts.length >= 5) {
                System.out.println("  Time:      " + parts[0]);
                System.out.println("  Latitude:  " + parts[1]);
                System.out.println("  Longitude: " + parts[2]);
                System.out.println("  Accuracy:  " + parts[3]);
                System.out.println("  Altitude:  " + parts[4]);
            } else {
                System.out.println("  Raw: " + locationLog.get(i));
            }
            System.out.println("-----------------------------------------");
        }
    }

    public static void clearLog() {
        locationLog.clear();
        System.out.println("Location log cleared.");
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
