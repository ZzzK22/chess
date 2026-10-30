package dataaccess;

import server.*;

import java.util.Collection;

public interface DataAccess {
    ErrorResponses deleteAll();

    ResponseData<UserData> getUserByUsername(String username);
    ResponseData<UserData> getUserByEmail(String email);

    ErrorResponses createUser(UserData userData);

    ResponseData<AuthData> createAuthToken(String username);
    ResponseData<AuthData> getAuthData(String token);

    ErrorResponses logout(String authToken);

    ResponseData<GameData> addGame(String gameName);
    ResponseData<Collection<GameData>> getGames();
    ResponseData<GameData> getGameData(int gameID);
    ErrorResponses joinGame(String username, boolean isWhite, int gameID);
}
