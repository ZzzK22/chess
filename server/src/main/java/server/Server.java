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
        UserData userData = json.fromJson(ctx.body(), UserData.class);
        ResponseData response = Service.register(userData);
        switch (response.error()){
            case null:
                ctx.status(200);
                ctx.result(json.toJson(response));
                break;
            case InvalidUsername:
                ctx.status(400);
                ctx.result("{\"message\": \"Username already taken!\"}");
                break;
            case InvalidEmail:
                ctx.status(400);
                ctx.result("{\"message\": \"You've already signed up with that email!\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Something went wrong!\"}");
        }
    }

    private static void delete(@NotNull Context ctx){
        ctx.result("{}");
        ctx.status(203);
        Service.deleteAll();
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
