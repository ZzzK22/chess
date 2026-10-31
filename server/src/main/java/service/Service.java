package service;

import dataaccess.MariadbDataAccess;
import org.mindrot.jbcrypt.BCrypt;
import server.*;
import dataaccess.DataAccess;

import java.util.Collection;
import java.util.Objects;

public class Service {
    static DataAccess dataAccess = new MariadbDataAccess();
    static{
        dataAccess.start();
    }


    public static ErrorResponses deleteAll(){
        return dataAccess.deleteAll();
    }

    public static ResponseData<AuthData> register(UserData userData) {
        ResponseData<UserData> userFromEmail = dataAccess.getUserByEmail(userData.email());
        ResponseData<UserData> userFromUsername = dataAccess.getUserByUsername(userData.username());
        if(userFromEmail.error() != null){
            return new ResponseData<>(userFromEmail.error(), null);
        }
        if(userFromUsername.error() != null){
            return new ResponseData<>(userFromUsername.error(), null);
        }
        if(userFromEmail.data() != null){
            return new ResponseData<>(ErrorResponses.InvalidEmail, null);
        }
        if (userFromUsername.data() != null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        UserData newUserData = new UserData(userData.username(), hashPassword(userData.password()), userData.email());
        ErrorResponses error = dataAccess.createUser(newUserData);
        if(error != null){
            return new ResponseData<>(error, null);
        }
        return dataAccess.createAuthToken(userData.username());
    }

    private static String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static ErrorResponses logout(String authToken){
        return dataAccess.logout(authToken);
    }

    public static ResponseData<AuthData> login(UserData userData) {
        ResponseData<UserData> userResponse = dataAccess.getUserByUsername(userData.username());
        if(userResponse.error() != null){
            return new ResponseData<>(userResponse.error(), null);
        }
        UserData data = userResponse.data();
        if(userData.username() == null || data == null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        if(!BCrypt.checkpw(userData.password(), data.password())){
            return new ResponseData<>(ErrorResponses.InvalidPassword, null);
        }
        return dataAccess.createAuthToken(data.username());

    }

    public static ResponseData<Collection<GameData>> listGames(String authorization) {
        ResponseData<AuthData> responseData = dataAccess.getAuthData(authorization);
        if(responseData.error() != null){
            return new ResponseData<>(responseData.error(), null);
        }
        if(responseData.data() == null){
            return new ResponseData<>(ErrorResponses.InvalidAuthToken, null);
        }
        return dataAccess.getGames(); // getGames returns a ResponseData
    }

    public static ResponseData<GameData> createGame(String authorization, String gameName) {
        ResponseData<AuthData> responseData = dataAccess.getAuthData(authorization);
        if(responseData.error() != null){
            return new ResponseData<>(responseData.error(), null);
        }
        if(responseData.data() == null){
            return new ResponseData<>(ErrorResponses.InvalidAuthToken, null);
        }

        return dataAccess.addGame(gameName);
    }

    public static ErrorResponses joinGame(String authorization, boolean isWhite, int gameID) {
        ResponseData<AuthData> authResponse = dataAccess.getAuthData(authorization);
        if(authResponse.error() != null){
            return authResponse.error();
        }
        if(authResponse.data() == null){
            return ErrorResponses.InvalidAuthToken;
        }
        GameData data = dataAccess.getGameData(gameID).data();
        if (data == null){
            return ErrorResponses.GameExistsnt;
        }
        if(isWhite && data.whiteUsername() != null || !isWhite && data.blackUsername() != null){
            return ErrorResponses.InvalidColor;
        }
        return dataAccess.joinGame(authResponse.data().username(), isWhite, gameID);
    }
}
