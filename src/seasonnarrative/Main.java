package seasonnarrative;

import seasonnarrative.visual.GraphicPane;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFrame gui = new JFrame("Season Narrative");
        gui.add(new GraphicPane());
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.setVisible(true);
        gui.setSize(500,500);
    }
}
