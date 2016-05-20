package backend;

public class Login {
    private static final String LOGIN = "root", PASSWORD = "toor";

    public static boolean authenticate(String username, String password) {
        return username.equals(LOGIN) && password.equals(PASSWORD);
    }
}