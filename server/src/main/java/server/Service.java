package server;

public class Service {
    static DataAccess dataAccess = new MemoryDataAccess();
    public static void deleteAll(){
        dataAccess.deleteAll();
    }

    public static ResponseData register(UserData userData) {
        if(dataAccess.getUserByEmail(userData.email()) != null){
            return new ResponseData(ErrorResponses.InvalidEmail, "");
        }
        if (dataAccess.getUserByUsername(userData.username()) != null){
            return new ResponseData(ErrorResponses.InvalidUsername, "");
        }
        dataAccess.createUser(userData);
        AuthData authToken = dataAccess.createAuthToken(userData.username());
        return new ResponseData(null, authToken.token());
    }
}
