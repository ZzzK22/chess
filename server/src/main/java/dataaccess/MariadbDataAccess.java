package dataaccess;

import server.AuthData;
import server.GameData;
import server.UserData;

import java.util.Collection;
import java.util.List;

public class MariadbDataAccess implements DataAccess{

    MariadbDataAccess(){

    }

    public void deleteAll() {

    }

    public UserData getUserByUsername(String username) {
        return null;
    }

    public UserData getUserByEmail(String email) {
        return null;
    }

    public void createUser(UserData userData) {

    }

    public AuthData createAuthToken(String username) {
        return null;
    }

    public AuthData getAuthData(String token) {
        return null;
    }

    public boolean logout(String authToken) {
        return false;
    }

    public GameData addGame(String gameName) {
        return null;
    }

    public Collection<GameData> getGames() {
        return List.of();
    }

    public GameData getGameData(int gameID) {
        return null;
    }

    public void joinGame(String username, boolean isWhite, int gameID) {

    }
}
