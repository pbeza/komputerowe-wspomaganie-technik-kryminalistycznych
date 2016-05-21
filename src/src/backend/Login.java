package backend;

import java.util.logging.Logger;

public class Login {
    private static final Logger log = Log.getLogger();
    private static final String LOGIN = "root", PASSWORD = "toor";

    public static boolean authenticate(String username, String password) {
        final boolean wasLoggedIn = username.equals(LOGIN) && password.equals(PASSWORD);
        final String msg = wasLoggedIn ? " user successfully logged in."
                : " user failed to log in. Exiting from application.";
        log.info(LOGIN + msg);
        return wasLoggedIn;
    }
}