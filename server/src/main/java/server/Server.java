package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", Server::delete);
        javalin.post("/user", Server::register);
        // Register your endpoints and exception handlers here.

    }

    private static void register(@NotNull Context ctx){
        Gson json = new Gson();
        UserData userData = null;
        userData = json.fromJson(ctx.body(), UserData.class);
        ErrorResponses response = Service.register(userData);
        switch (response){
            case null:
                ctx.status(200);
                break;
            case InvalidUsername:
                ctx.status(403);
                break;
            case InvalidEmail:
                ctx.status(403);
                break;
            default:
                ctx.status(500);
        }
    }

    private static void delete(@NotNull Context ctx){
        handle(ctx);
        Service.deleteAll();
    }

    private static void handle(@NotNull Context ctx){
        var r = Map.of();
        var json = new Gson();
        ctx.json(json.toJson(r));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
