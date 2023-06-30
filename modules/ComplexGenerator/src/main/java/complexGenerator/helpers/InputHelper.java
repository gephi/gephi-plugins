package complexGenerator.helpers;

import javax.swing.*;

public class InputHelper {

    public static int InputIntValue(String message, String notIntMessage){
        boolean valid = false;
        int param = 0;
        while(!valid){
            String input = JOptionPane.showInputDialog(message);
            try {
                param = Integer.parseInt(input);
                valid = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, notIntMessage);
            }
        }
        return param;
    }

    public static int InputIntValue(String message){
        return InputHelper.InputIntValue(message, "Proszę wprowadzić poprawną liczbę całkowitą.");
    }

    public static double InputDoubleValue(String message, String notIntMessage){
        boolean valid = false;
        double param = 0.0;
        while(!valid){
            String input = JOptionPane.showInputDialog(message);
            try {
                param = Double.parseDouble(input);
                valid = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, notIntMessage);
            }
        }
        return param;
    }

    public static double InputDoubleValue(String message){
        return InputHelper.InputDoubleValue(message, "Proszę wprowadzić poprawną liczbę zmienno przecinkową.");
    }
}
