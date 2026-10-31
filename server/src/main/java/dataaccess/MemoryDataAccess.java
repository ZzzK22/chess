package dataaccess;

import chess.ChessGame;
import server.*;

import java.util.HashMap;
import java.util.Collection;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> userData = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private HashMap<Integer, GameData> games = new HashMap<>();
    private int authTokenizer = 0;
    public ErrorResponses deleteAll(){
        userData = new HashMap<>();
        authData = new HashMap<>();
        games = new HashMap<>();
        return null;
    }

    public ErrorResponses start() {
        return null;
    }

    public ResponseData<UserData> getUserByUsername(String username) {
        return new ResponseData<>(null, userData.get(username));
    }

    public ResponseData<UserData> getUserByEmail(String email) {
        for(var i : userData.entrySet()){
            if(i.getValue().email().equals(email)){
                return new ResponseData<>(null, i.getValue());
            }
        }
        return new ResponseData<>(null, null);
    }

    private static String authStringFromInt(int val){
        return "Auth: " + val;
    }

    public ErrorResponses createUser(UserData userData) {
        this.userData.put(userData.username(), userData);
        return null;
    }

    public ResponseData<AuthData> createAuthToken(String username) {
        String authToken = authStringFromInt(++authTokenizer);
        AuthData authData = new AuthData(username, authToken);
        this.authData.put(authToken, authData);
        return new ResponseData<>(null, authData);
    }

    public ResponseData<AuthData> getAuthData(String token){
        for(var i : authData.entrySet()){
            if(i.getValue().authToken().equals(token)){
                return new ResponseData<>(null, i.getValue());
            }
        }
        return new ResponseData<>(null, null);
    }

    public ResponseData<Collection<GameData>> getGames() {
        return new ResponseData<>(null, games.values());
    }

    public ResponseData<GameData> getGameData(int gameID) {
        return new ResponseData<>(null, games.get(gameID));
    }

    public ErrorResponses joinGame(String username, boolean isWhite, int gameID) {
        GameData game = games.get(gameID);
        GameData newGame;
        if(isWhite) {
            newGame = new GameData(game.game(), username, game.blackUsername(), game.gameName(), game.gameID());
        } else {
            newGame = new GameData(game.game(), game.whiteUsername(), username, game.gameName(), game.gameID());
        }
        games.put(gameID, newGame);
        return null;
    }

    public ErrorResponses logout(String authToken) {
        AuthData data = this.getAuthData(authToken).data();
        if (data == null) {
            return ErrorResponses.InvalidAuthToken;
        }
        authData.remove(data.authToken());
        return null;
    }

    public ResponseData<GameData> addGame(String gameName) {
        int id = getGameID();
        GameData data = new GameData(new ChessGame(), null, null, gameName, id);
        games.put(id, data);
        return new ResponseData<>(null, data);
    }

    static int id = 0;
    private int getGameID() {
        return ++id;
    }
}
