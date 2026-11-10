package client;

import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class REPL {
    private static String username = null;
    private static String authToken = null;
    private static boolean loggedIn = false;
    private static ServerFacade serverFacade;
    public static void run (int port){
        serverFacade = new ServerFacade(port);
        Scanner stdin = new Scanner(System.in);
        while(true) {
            loggedIn = username != null;
            System.out.print("[" + (Optional.ofNullable(username).orElse("LOGGED OUT")) + "] $:: ");
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
                continue;
            }
            login("username", "password");
            System.out.println(in);
        }

    }

    private static String login(String username, String password){
        try {
            return serverFacade.login(username, password);
        } catch (Exception _) {
        }
        return null;
    }

    private static boolean theyHavePut(Object in, Object b){
        return Objects.equals(in, b);
    }

    private static void logout() {
        serverFacade.logout(authToken);
    }

    private static void printColoredOption(String blueText, String blackText){
        System.out.print(SET_TEXT_COLOR_BLUE);
        System.out.print(blueText);
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(" - ");
        System.out.print(blackText);
        System.out.println();
    }

    private static void displayLoggedOutOptions(){
        printColoredOption("register <USERNAME> <PASSWORD> <EMAIL>", "create an account");
        printColoredOption("login <USERNAME> <PASSWORD>", "log in to your account");
        printColoredOption("quit", "quit the program");
        printColoredOption("help", "display this help screen");
    }

    private static void displayLoggedInOptions() {
        printColoredOption("create <NAME>", "create game");
        printColoredOption("list", "list games");
        printColoredOption("join <ID> (WHITE|BLACK)", "join game");
        printColoredOption("observe <ID>", "observe game");
        printColoredOption("logout", "log out of your account");
        printColoredOption("quit", "quit the program");
        printColoredOption("help", "display this help screen");
    }
}
