/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package levallois.clement.utils;

/**
 *
 * @author C. Levallois
 */
public class Clock {

    private long start;
    private String action;
    private String logText;
    private final String newLine = "\n";
    private final String interval = "-------------------------------\n\n";

    public Clock(String action) {
        this.action = action;
        startClock();
    }

    void startClock() {

        start = System.currentTimeMillis();
        logText = action + "..." + newLine;
        //GUI_Screen_1.logArea.setText(GUI_Screen_1.logArea.getText().concat(logText));
        //GUI_Screen_1.logArea.setCaretPosition(GUI_Screen_1.logArea.getText().length());

        //GUI_Screen_1.logArea.repaint();

        System.out.print(logText);
    }

    public void printElapsedTime() {

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - start;

        if (elapsedTime
                < 1000) {
            System.out.println("still " + action.toLowerCase() + ", " + elapsedTime + " milliseconds]");

        } else {
            System.out.println("still " + action.toLowerCase() + ", " + elapsedTime / 1000 + " seconds]");
        }

    }

    public long getElapsedTime() {

        long currentTime = System.currentTimeMillis();
        return (currentTime - start);

    }

    public void closeAndPrintClock() {

        long currentTime = System.currentTimeMillis();
        long totalTime = currentTime - start;

        if (totalTime
                < 1000) {
            logText = "finished [took: " + totalTime + " milliseconds]" + newLine + interval;
            System.out.print(logText);
        } else if (totalTime < 10000) {
            logText = "finished [took: " + totalTime / 1000 + " seconds] (" + totalTime + " ms)" + newLine + interval;
            System.out.print(logText);
        } else if (totalTime < 60000) {
            logText = "finished [took: " + totalTime / 1000 + " seconds]" + newLine + interval;
            System.out.print(logText);
        } else {
            logText = "finished [took: " + totalTime / 60000 + " minutes " + Math.round((totalTime % 60000) / 1000) + " seconds]" + newLine + interval;
            System.out.print(logText);

        }
        //GUI_Screen_1.logArea.setText(GUI_Screen_1.logArea.getText().concat(logText));
        //GUI_Screen_1.logArea.setCaretPosition(GUI_Screen_1.logArea.getText().length());



    }
}
