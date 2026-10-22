package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", Server::delete);
        javalin.post("/user", Server::register);
        javalin.delete("/session", Server::logout);
        javalin.post("/session", Server::login);
        javalin.get("/game", Server::listGames);
        // Register your endpoints and exception handlers here.

    }

    private static void listGames(@NotNull Context ctx) {
        ResponseData<List<GameData>> response = Service.listGames(ctx.header("authorization"));

        ctx.result(new Gson().toJson(response.data()));
        checkAuthorization(ctx, response.error());
    }

    private static void logout(@NotNull Context ctx){
        ErrorResponses error = Service.logout(ctx.header("authorization"));
        ctx.status(203);
        checkAuthorization(ctx, error);
    }

    private static void login(@NotNull Context ctx){
        Gson json = new Gson();
        UserData userData = json.fromJson(ctx.body(), UserData.class);
        ResponseData<AuthData> response = Service.login(userData);
        switch (response.error()){
            case null:
                ctx.status(200);
                ctx.result(json.toJson(response));
                break;
            case InvalidUsername:
                ctx.status(400);
                ctx.result("{\"message\": \"Invalid username!\"}");
                break;
            case InvalidPassword:
                ctx.status(400);
                ctx.result("{\"message\": \"Invalid password!\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Something went wrong!\"}");
        }
    }

    private static void register(@NotNull Context ctx){
        Gson json = new Gson();
        UserData userData = json.fromJson(ctx.body(), UserData.class);
        ResponseData<String> response = Service.register(userData);
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

    private static void checkAuthorization(@NotNull Context ctx, ErrorResponses error) {
        switch (error){
            case null:
                // use the defaults set prior to calling this function
                break;
            case InvalidAuthToken:
                ctx.status(401);
                ctx.result("{\"message\": \"Invalid authorization! (likely a duplicate request)\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Something went wrong!\"}");
        }
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
