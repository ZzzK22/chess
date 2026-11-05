package client;

import java.util.Objects;
import java.util.Scanner;

public class REPL {
    private static String username = null;
    private static String authToken = null;
    private static boolean loggedIn = false;
    public static void run (){
        Scanner stdin = new Scanner(System.in);
        while(true) {
            loggedIn = username != null;
            System.out.print("[" + (username == null ? "LOGGED OUT" : username) + "] $:: ");
            String in = stdin.next();
            if(theyHavePut(in, "quit")){
                if(loggedIn){
                    logout();
                }
                break;
            }
            if(theyHavePut(in, "help")){
                if(loggedIn){
                    displayLoggedInOptions();
                } else {
                    displayLoggedOutOptions();
                }
            }
            System.out.println(in);
        }

    }

    private static boolean theyHavePut(Object in, Object b){
        return Objects.equals(in, b);
    }

    private static void logout() {
        ServerFacade.logout(authToken);
    }

    private static void displayLoggedOutOptions(){
        System.out.println("");
    }

    private static void displayLoggedInOptions() {
    }
}
