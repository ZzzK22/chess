package server;

import java.util.List;

public interface DataAccess {
    public void deleteAll();

    UserData getUserByUsername(String username);
    UserData getUserByEmail(String email);

    void createUser(UserData userData);

    AuthData createAuthToken(String username);
    AuthData getAuthData(String token);

    boolean logout(String authToken);

    List<GameData> getGames();
}
