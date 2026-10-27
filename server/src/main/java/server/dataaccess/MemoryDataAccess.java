package server.dataaccess;

import chess.ChessGame;
import server.AuthData;
import server.GameData;
import server.UserData;

import java.util.HashMap;
import java.util.Collection;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> userData = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private HashMap<Integer, GameData> games = new HashMap<>();
    private int authTokenizer = 0;
    public void deleteAll(){
        userData = new HashMap<>();
        authData = new HashMap<>();
        games = new HashMap<>();
    }

    public UserData getUserByUsername(String username) {
        return userData.get(username);
    }

    public UserData getUserByEmail(String email) {
        for(var i : userData.entrySet()){
            if(i.getValue().email().equals(email)){
                return i.getValue();
            }
        }
        return null;
    }

    private static String authStringFromInt(int val){
        return "Auth: " + val;
    }

    public void createUser(UserData userData) {
        this.userData.put(userData.username(), userData);
    }

    public AuthData createAuthToken(String username) {
        String authToken = authStringFromInt(++authTokenizer);
        AuthData authData = new AuthData(username, authToken);
        this.authData.put(authToken, authData);
        return authData;
    }

    public AuthData getAuthData(String token){
        for(var i : authData.entrySet()){
            if(i.getValue().authToken().equals(token)){
                return i.getValue();
            }
        }
        return null;
    }

    public Collection<GameData> getGames() {
        return games.values();
    }

    public GameData getGameData(int gameID) {
        return games.get(gameID);
    }

    public void joinGame(String username, boolean isWhite, int gameID) {
        GameData game = games.get(gameID);
        GameData newGame;
        if(isWhite) {
            newGame = new GameData(game.game(), username, game.blackUsername(), game.gameName(), game.gameID());
        } else {
            newGame = new GameData(game.game(), game.whiteUsername(), username, game.gameName(), game.gameID());
        }
        games.put(gameID, newGame);
    }

    public boolean logout(String authToken) {
        AuthData data = this.getAuthData(authToken);
        if (data == null) {
            return false;
        }
        authData.remove(data.authToken());
        return true;
    }

    public GameData addGame(String gameName) {
        int id = getGameID();
        GameData data = new GameData(new ChessGame(), null, null, gameName, id);
        games.put(id, data);
        return data;
    }

    static int id = 0;
    private int getGameID() {
        return ++id;
    }
}
