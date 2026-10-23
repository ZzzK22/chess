package server;

import java.util.Collection;
import java.util.Objects;

public class Service {
    static DataAccess dataAccess = new MemoryDataAccess();
    public static void deleteAll(){
        dataAccess.deleteAll();
    }

    public static ResponseData<AuthData> register(UserData userData) {
        if(dataAccess.getUserByEmail(userData.email()) != null){
            return new ResponseData<>(ErrorResponses.InvalidEmail, null);
        }
        if (dataAccess.getUserByUsername(userData.username()) != null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        dataAccess.createUser(userData);
        AuthData authToken = dataAccess.createAuthToken(userData.username());
        return new ResponseData<>(null, authToken);
    }

    public static ErrorResponses logout(String authToken){
        if(dataAccess.logout(authToken)){
            return null;
        }
        return ErrorResponses.InvalidAuthToken;
    }

    public static ResponseData<AuthData> login(UserData userData) {
        UserData data = dataAccess.getUserByUsername(userData.username());
        if(userData.username() == null || data == null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, null);
        }
        if(!Objects.equals(data.password(), userData.password())){
            return new ResponseData<>(ErrorResponses.InvalidPassword, null);
        }
        return new ResponseData<>(null,
                dataAccess.createAuthToken(data.username()));

    }

    public static ResponseData<Collection<GameData>> listGames(String authorization) {
        if(dataAccess.getAuthData(authorization) == null){
            return new ResponseData<>(ErrorResponses.InvalidAuthToken, null);
        }
        return new ResponseData<>(null, dataAccess.getGames());
    }

    public static ResponseData<GameData> createGame(String authorization, String gameName) {
        if(dataAccess.getAuthData(authorization) == null){
            return new ResponseData<>(ErrorResponses.InvalidAuthToken, null);
        }

        GameData data = dataAccess.addGame(gameName);

        return new ResponseData<>(null, data);
    }

    public static ErrorResponses joinGame(String authorization, boolean isWhite, int gameID) {
        if(dataAccess.getAuthData(authorization) == null){
            return ErrorResponses.InvalidAuthToken;
        }
        GameData data = dataAccess.getGameData(gameID);
        if (data == null){
            return ErrorResponses.GameExistsnt;
        }
        if(isWhite && data.whiteUsername() != null || !isWhite && data.blackUsername() != null){
            return ErrorResponses.InvalidColor;
        }
        dataAccess.joinGame(dataAccess.getAuthData(authorization).username(), isWhite, gameID);
        return null;
    }
}
