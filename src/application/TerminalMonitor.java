package application;

import java.util.ArrayList;

/**
 * @author Azeez G. Shola
 * @version 1.0
 */
public class TerminalMonitor {

    /**
     * terminal monitor to track the system
     * a singleton object used throughout the application
     */
    private static TerminalMonitor single_instance = null;
    /**
     * list of string to show the message
     */
    private ArrayList<String> toPrintInTerminal;

    /**
     * private monitor for singleton
     */
    private TerminalMonitor() {
        toPrintInTerminal = new ArrayList<>();
    }

    /**
     * get all the message from terminal
     *
     * @return list of strings
     */
    public ArrayList<String> getToPrintInTerminal() {
        return toPrintInTerminal;
    }

    /**
     * instance of terminal monitor
     *
     * @return instance of terminal monitor
     */
    public static TerminalMonitor getInstance() {
        if (single_instance == null)
            single_instance = new TerminalMonitor();

        return single_instance;
    }

    /**
     * add message to terminal
     *
     * @param message passed to the terminal
     */
    public void addNewMessage(String message) {
        toPrintInTerminal.add(message);
    }
}
