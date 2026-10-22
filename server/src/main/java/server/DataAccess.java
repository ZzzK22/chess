package server;

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
}
