package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.Service;

import java.util.Collection;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        javalin.delete("/db", Server::delete);
        javalin.post("/user", Server::register);
        javalin.delete("/session", Server::logout);
        javalin.post("/session", Server::login);
        javalin.get("/game", Server::listGames);
        javalin.post("/game", Server::createGame);
        javalin.put("/game", Server::joinGame);
        // Register your endpoints and exception handlers here.

    }

    private static void joinGame(@NotNull Context ctx) {
        JoinGameData data = new Gson().fromJson(ctx.body(), JoinGameData.class);
        if(data.gameID() == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Invalid game ID!\"}");
            return;
        }
        if(data.playerColor() == null || !data.playerColor().equals("WHITE") && !data.playerColor().equals("BLACK")){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Invalid player color!\"}");
            return;
        }
        ErrorResponses error = Service.joinGame(ctx.header("authorization"), data.playerColor().equals("WHITE"), data.gameID());
        checkAuthorization(ctx, error);
        if(error == ErrorResponses.InvalidColor){
            ctx.status(403);
            ctx.result("{\"message\": \"Error: Player color already taken!\"}");
        }
    }

    private static void createGame(@NotNull Context ctx) {
        String gameName = new Gson().fromJson(ctx.body(), GameData.class).gameName();
        if(gameName == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: gameName is a required field!\"}");
            return;
        }
        ResponseData<GameData> response = Service.createGame(ctx.header("authorization"), gameName);

        ctx.status(200);
        if(response.error() == null){
            ctx.result(String.format("{\"gameID\": %d}", response.data().gameID()));
        }
        checkAuthorization(ctx, response.error());
    }

    private static void listGames(@NotNull Context ctx) {
        ResponseData<Collection<GameData>> response = Service.listGames(ctx.header("authorization"));

        ctx.result(String.format("{\"games\": %s}", new Gson().toJson(response.data())));
        checkAuthorization(ctx, response.error());
    }

    private static void logout(@NotNull Context ctx){
        ErrorResponses error = Service.logout(ctx.header("authorization"));
        ctx.status(200);
        ctx.result("{}");
        checkAuthorization(ctx, error);
    }

    private static void login(@NotNull Context ctx){
        Gson json = new Gson();
        UserData userData = json.fromJson(ctx.body(), UserData.class);
        if(userData.username() == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Username is a required field! (register)\"}");
            return;
        }
        if(userData.password() == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Password is a required field! (register)\"}");
            return;
        }
        ResponseData<AuthData> response = Service.login(userData);
        switch (response.error()){
            case null:
                ctx.status(200);
                ctx.result(json.toJson(response.data()));
                return;
            case InvalidUsername:
                ctx.status(401);
                ctx.result("{\"message\": \"Error: Invalid username! (login)\"}");
                break;
            case InvalidPassword:
                ctx.status(401);
                ctx.result("{\"message\": \"Error: Invalid password! (login)\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Error: Something went wrong!\"}");
        }
    }

    private static void register(@NotNull Context ctx){
        Gson json = new Gson();
        UserData userData = json.fromJson(ctx.body(), UserData.class);
        if(userData.username() == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Username is a required field! (register)\"}");
            return;
        }
        if(userData.password() == null){
            ctx.status(400);
            ctx.result("{\"message\": \"Error: Password is a required field! (register)\"}");
            return;
        }
        ResponseData<AuthData> response = Service.register(userData);
        switch (response.error()){
            case null:
                ctx.status(200);
                ctx.result(json.toJson(response.data()));
                break;
            case InvalidUsername:
                ctx.status(403);
                ctx.result("{\"message\": \"Error: Username already taken! (register)\"}");
                break;
            case InvalidEmail:
                ctx.status(403);
                ctx.result("{\"message\": \"Error: You've already signed up with that email! (register)\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Error: Something went wrong! (register)\"}");
        }
    }

    private static void delete(@NotNull Context ctx){
        ctx.result("{}");
        ctx.status(200);
        Service.deleteAll();
    }

    private static void checkAuthorization(@NotNull Context ctx, ErrorResponses error) {
        switch (error){
            case null:
                // use the defaults set prior to calling this function
                break;
            case InvalidAuthToken:
                ctx.status(401);
                ctx.result("{\"message\": \"Error: Invalid authorization!\"}");
                break;
            default:
                ctx.status(500);
                ctx.result("{\"message\": \"Error: Something went wrong!\"}");
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
