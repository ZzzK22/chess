package server;

public interface DataAccess {
    public void deleteAll();

    UserData getUserByUsername(String username);

    UserData getUserByEmail(String email);

    void createUser(UserData userData);

    AuthData createAuthToken(String username);
    AuthData getAuthData(String username);

    boolean logout(String authToken);
}
