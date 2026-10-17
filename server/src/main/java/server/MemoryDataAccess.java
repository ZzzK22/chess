package server;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    HashMap<String, UserData> map = new HashMap<>();
    public void deleteAll(){
        HashMap<String, UserData> map = new HashMap<>();
    }

    public UserData getUserByUsername(String username) {
        return map.get(username);
    }

    public UserData getUserByEmail(String email) {
        for(var i : map.entrySet()){
            if(i.getValue().email().equals(email)){
                return i.getValue();
            }
        }
        return null;
    }

    public void createUser(UserData userData) {
        map.put(userData.username(), userData);
    }
}
