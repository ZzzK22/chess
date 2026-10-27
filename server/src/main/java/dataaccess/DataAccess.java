package dataaccess;

import server.AuthData;
import server.GameData;
import server.UserData;

import java.util.Collection;

public interface DataAccess {
    public void deleteAll();

    UserData getUserByUsername(String username);
    UserData getUserByEmail(String email);

    void createUser(UserData userData);

    AuthData createAuthToken(String username);
    AuthData getAuthData(String token);

    boolean logout(String authToken);

    GameData addGame(String gameName);
    Collection<GameData> getGames();
    GameData getGameData(int gameID);
    void joinGame(String username, boolean isWhite, int gameID);
}
