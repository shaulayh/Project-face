package application;

import java.util.ArrayList;

public class TerminalMonitor {

    private static TerminalMonitor single_instance = null;
    private ArrayList<String> toPrintInTerminal;

    private TerminalMonitor() {
        toPrintInTerminal = new ArrayList<>();
    }


    public ArrayList<String> getToPrintInTerminal() {
        return toPrintInTerminal;
    }


    public static TerminalMonitor getInstance() {
        if (single_instance == null)
            single_instance = new TerminalMonitor();

        return single_instance;
    }

    public void setToPrintInTerminal(ArrayList<String> toPrintInTerminal) {
        this.toPrintInTerminal = toPrintInTerminal;
    }

    public void addNewMessage(String message) {
        toPrintInTerminal.add(message);
    }
}
