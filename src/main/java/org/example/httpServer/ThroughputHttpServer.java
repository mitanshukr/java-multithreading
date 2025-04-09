package org.example.httpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThroughputHttpServer {
    private static final int NUMBER_OF_THREADS = 20;
    private static final String INPUT_FILE = "./src/main/resources/war_and_peace.txt";

    public static void main(String[] args) throws IOException {
//        System.out.println("Working directory: " + System.getProperty("user.dir"));
        String textFile = new String(Files.readAllBytes(Paths.get(INPUT_FILE)));
        startServer(textFile);
    }

    public static void startServer(String textFile) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/search", new WordCountHandler(textFile));

        Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        server.setExecutor(executor);
        server.start();
    }

    private record WordCountHandler(String textFile) implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String query = httpExchange.getRequestURI().getQuery();
            String[] keyVal = query.split("=");
            String action = keyVal[0];
            String word = keyVal[1];
            if (!action.equals("word")) {
                String response = "{\"error\": \"Invalid query parameter. Expected 'word=<value>'\"}";
                httpExchange.getResponseHeaders().set("Content-Type", "application/json");

                httpExchange.sendResponseHeaders(400, response.getBytes().length);
                OutputStream stream = httpExchange.getResponseBody();
                stream.write(response.getBytes());
                stream.close();
                return;
            }

            // count words
            long count = countWord(word);

//            byte[] response = Long.toString(count).getBytes();
            String jsonResponse = "{\"count\": " + count + ", \"word\": \"" + word + "\"}";
            httpExchange.getResponseHeaders().set("Context-Type", "application/json");

            httpExchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            OutputStream stream = httpExchange.getResponseBody();
            stream.write(jsonResponse.getBytes());
            stream.close();
        }

        private long countWord(String word) {
            long count = 0;
            int idx = 0;
            while (idx >= 0) {
                idx = textFile.indexOf(word, idx);

                if (idx >= 0) {
                    count++;
                    idx++;
                }
            }
            return count;
        }
    }


}
