package server;

public interface DataAccess {
    public void deleteAll();

    UserData getUserByUsername(String username);

    UserData getUserByEmail(String email);

    void createUser(UserData userData);
}
