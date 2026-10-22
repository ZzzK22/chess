package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> userData = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
    private ArrayList<GameData> games = new ArrayList<>();
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

    public List<GameData> getGames() {
        return games;
    }

    public boolean logout(String authToken) {
        AuthData data = this.getAuthData(authToken);
        if (data == null) {
            return false;
        }
        authData.remove(data.username());
        return true;
    }
}
