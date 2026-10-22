package server;

import chess.ChessGame;

import java.util.HashMap;
import java.util.Collection;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> userData = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private HashMap<Integer, GameData> games = new HashMap<>();
    private int authTokenizer = 0;
    public void deleteAll(){
        userData = new HashMap<>();
        authData = new HashMap<>();
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
        this.authData.put(username, authData);
        return authData;
    }

    public AuthData getAuthData(String token){
        for(var i : authData.entrySet()){
            if(i.getValue().token().equals(token)){
                return i.getValue();
            }
        }
        return null;
    }

    public Collection<GameData> getGames() {
        return games.values();
    }

    public boolean logout(String authToken) {
        AuthData data = this.getAuthData(authToken);
        if (data == null) {
            return false;
        }
        authData.remove(data.username());
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
