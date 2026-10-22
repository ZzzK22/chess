package server;

import java.util.Objects;

public class Service {
    static DataAccess dataAccess = new MemoryDataAccess();
    public static void deleteAll(){
        dataAccess.deleteAll();
    }

    public static ResponseData<String> register(UserData userData) {
        if(dataAccess.getUserByEmail(userData.email()) != null){
            return new ResponseData<>(ErrorResponses.InvalidEmail, "");
        }
        if (dataAccess.getUserByUsername(userData.username()) != null){
            return new ResponseData<>(ErrorResponses.InvalidUsername, "");
        }
        dataAccess.createUser(userData);
        AuthData authToken = dataAccess.createAuthToken(userData.username());
        return new ResponseData<>(null, authToken.token());
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
}
