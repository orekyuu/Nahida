package net.orekyuu.nahida.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ServerHandler implements HttpHandler {

    ObjectMapper mapper = new ObjectMapper();
    record Response(int code, Object body) {}

    Map<String, String> query(HttpExchange exchange) {
        URI uri = exchange.getRequestURI();
        return Arrays.stream(uri.getQuery().split("&"))
                .map(it -> it.split("="))
                .collect(Collectors.toMap(it -> it[0], it -> it[1]));
    }

    <T> T bodyJson(HttpExchange exchange, Class<T> clazz) throws IOException {
        try (var in = exchange.getRequestBody()) {
            String body = new String(in.readAllBytes());
            System.out.println(body);
            return mapper.readValue(body, clazz);
        }
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Response response = doHandle(exchange);

            String json = mapper.writeValueAsString(response.body);
            byte[] bytes = json.getBytes();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Access-Control-Allow-Origin", "*");
            headers.set("Access-Control-Allow-Methods", "*");
            headers.set("Access-Control-Allow-Headers", "*");

            exchange.sendResponseHeaders(response.code(), bytes.length);
            OutputStream body = exchange.getResponseBody();
            body.write(bytes);
            body.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    abstract Response doHandle(HttpExchange exchange) throws IOException;
}
