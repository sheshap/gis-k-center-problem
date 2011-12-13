package pl.elka.gis.core;

import pl.elka.gis.model.generator.DataGenerator;
import pl.elka.gis.model.generator.DataValidationException;
import pl.elka.gis.ui.MainFrame;

public class Main {

    public static void main(String[] args) {
        new MainFrame().setVisible(true);
        DataGenerator dg = new DataGenerator();
        try {
            dg.generateData();
            dg.saveData("graph_files/aaa.txt");
        } catch (DataValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
