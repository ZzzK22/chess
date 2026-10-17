package server;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    private HashMap<String, UserData> userData = new HashMap<>();
    private HashMap<String, AuthData> authData = new HashMap<>();
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

    private static String AuthStringFromInt(int val){
        return "Auth: " + val;
    }

    public void createUser(UserData userData) {
        this.userData.put(userData.username(), userData);
    }

    public AuthData createAuthToken(String username) {
        String authToken = AuthStringFromInt(++authTokenizer);
        AuthData authData = new AuthData(username, authToken);
        this.authData.put(username, authData);
        return authData;
    }

    public AuthData getAuthData(String username) {
        return authData.get(username);
    }
}
