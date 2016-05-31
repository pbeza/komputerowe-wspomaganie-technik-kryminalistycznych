package backend;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log extends Logger {

    private final static String LOG_DIR = "logs/", LOG_PATH = LOG_DIR
            + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date())
            + ".log";
    private final static Level LOGGER_LOG_LEVEL = Level.FINEST,
            FILE_LOG_LEVEL = Level.FINEST, CONSOLE_LOG_LEVEL = Level.FINEST;
    private final static Log eigenfacesLogger = new Log();
    private FileHandler fileHandler;
    private Handler consoleHandler;

    private Log() {
        super("eigenfaces", null);
        File dir = new File(LOG_DIR);
        dir.mkdir();
        setLevel(LOGGER_LOG_LEVEL);
        SimpleFormatter formatter = new SimpleFormatter();
        addLogFileHandler(formatter);
        addLogConsoleHandler(formatter);
    }

    public static Log getLogger() {
        return eigenfacesLogger;
    }

    public void closeHandlers() {
        fileHandler.close(); // to remove temporary *.lck file
        consoleHandler.close();
    }

    private void addLogFileHandler(Formatter formatter) {
        try {
            fileHandler = new FileHandler(LOG_PATH);
        } catch (SecurityException | IOException e) {
            final String errMsg = "Error during setting up logger. Logs won't be redirected to file. Details: "
                    + e.getMessage();
            config(errMsg);
        }
        if (fileHandler != null) {
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(FILE_LOG_LEVEL);
            addHandler(fileHandler);
        }
    }

    private void addLogConsoleHandler(Formatter formatter) {
        // ConsoleHandler prints using scary red color ;(
        consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        consoleHandler.setLevel(CONSOLE_LOG_LEVEL);
        addHandler(consoleHandler);
    }
}
