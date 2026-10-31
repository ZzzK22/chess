package service;

import dataaccess.MariadbDataAccess;
import server.*;
import dataaccess.DataAccess;

import java.util.Collection;
import java.util.Objects;

public class Service {
    static DataAccess dataAccess = new MariadbDataAccess();
    static{
        dataAccess.start();
    }


    public static void deleteAll(){
        dataAccess.deleteAll();
    }

    public static ResponseData<AuthData> register(UserData userData) {
        if(dataAccess.getUserByEmail(userData.email()).data() != null){
            return new ResponseData<>(ErrorResponses.InvalidEmail, null);
        }
        if (dataAccess.getUserByUsername(userData.username()).data() != null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        dataAccess.createUser(userData);
        return dataAccess.createAuthToken(userData.username());
    }

    public static ErrorResponses logout(String authToken){
        return dataAccess.logout(authToken);
    }

    public static ResponseData<AuthData> login(UserData userData) {
        UserData data = dataAccess.getUserByUsername(userData.username()).data();
        if(userData.username() == null || data == null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        if(!Objects.equals(data.password(), userData.password())){
            return new ResponseData<>(ErrorResponses.InvalidPassword, null);
        }
        return dataAccess.createAuthToken(data.username());

    }

    public static ResponseData<Collection<GameData>> listGames(String authorization) {
        if(dataAccess.getAuthData(authorization).data() == null){
            return new ResponseData<>(ErrorResponses.InvalidAuthToken, null);
        }
        return dataAccess.getGames(); // getGames returns a ResponseData
    }

    public static ResponseData<GameData> createGame(String authorization, String gameName) {
        if(dataAccess.getAuthData(authorization).data() == null){
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
        dataAccess.joinGame(authResponse.data().username(), isWhite, gameID);
        return null;
    }
}
