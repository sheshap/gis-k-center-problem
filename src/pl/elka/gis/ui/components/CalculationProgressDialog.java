package pl.elka.gis.ui.components;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class CalculationProgressDialog extends JDialog {

    // TODO implement dialog UI and logic
    public CalculationProgressDialog(JFrame parentFrame, String title, boolean modal) {
        super(parentFrame, title, modal);
    }

    public void updateProgress(float progress) {
    }
}
