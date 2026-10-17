package server;

public class Service {
    static DataAccess dataAccess = new MemoryDataAccess();
    public static void deleteAll(){
        dataAccess.deleteAll();
    }

    public static ErrorResponses register(UserData userData) {
        if(dataAccess.getUserByEmail(userData.email()) != null){
            return ErrorResponses.InvalidEmail;
        }
        if (dataAccess.getUserByUsername(userData.username()) != null){
            return ErrorResponses.InvalidUsername;
        }
        dataAccess.createUser(userData);
        return null;
    }
}
