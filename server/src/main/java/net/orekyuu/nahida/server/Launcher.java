package net.orekyuu.nahida.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.orekyuu.nahida.domain.ClassFileSource;
import net.orekyuu.nahida.domain.JarFileSource;
import net.orekyuu.nahida.domain.structure.Class;
import net.orekyuu.nahida.domain.structure.ClassFQN;
import net.orekyuu.nahida.domain.workspace.CallerGraph;
import net.orekyuu.nahida.domain.workspace.MethodCallNode;
import net.orekyuu.nahida.domain.workspace.Project;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Launcher {
    public static void main(String[] args) throws IOException {
        Project project = new Project();
        for (String p : args) {
            if (p.endsWith(".jar")) {
                project.load(new JarFileSource(Path.of(p)));
            } else {
                project.load(new ClassFileSource(Path.of(p)));
            }
        }

        CallerGraph callGraph = project.createMethodCallGraph();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/api/classes", new ServerHandler() {
            @Override
            Response doHandle(HttpExchange exchange) throws IOException {
                System.out.println(String.join(" ", exchange.getRequestMethod(), exchange.getRequestURI().toString()));
                return new Response(200, project.allClass().stream().map(ClassJson::from).toList());
            }
        });
        httpServer.createContext("/api/methods", new ServerHandler() {
            @Override
            Response doHandle(HttpExchange exchange) throws IOException {
                System.out.println(String.join(" ", exchange.getRequestMethod(), exchange.getRequestURI().toString()));
                return Optional.ofNullable(query(exchange).get("class"))
                        .flatMap(param -> project.findClass(ClassFQN.fromClassFQN(param)))
                        .map(clazz -> {
                            var body = clazz.methods().stream().map(MethodJson::from).toList();
                            return new Response(200, body);
                        })
                        .orElse(new Response(404, new ErrorJson("not found")));
            }
        });

        httpServer.createContext("/api/methodCalls", new ServerHandler() {
            @Override
            Response doHandle(HttpExchange exchange) throws IOException {
                System.out.println(String.join(" ", exchange.getRequestMethod(), exchange.getRequestURI().toString()));
                if (exchange.getRequestMethod().equals("OPTIONS")) {
                    return new Response(200, "");
                }
                MethodSignatureJson signatureJson = bodyJson(exchange, MethodSignatureJson.class);
                MethodCallNode node = callGraph.getNode(signatureJson.toMethodSignature());
                CallGraphJson json = CallGraphJson.from(node);
                return new Response(200, json);
            }
        });
        httpServer.start();
    }
}
