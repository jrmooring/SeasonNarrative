package seasonnarrative;

import seasonnarrative.audio.Clip;
import seasonnarrative.audio.LineOut;
import seasonnarrative.visual.GraphicPane;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        JFrame gui = new JFrame("Season Narrative");
        gui.add(new GraphicPane());
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.setVisible(true);
        gui.setSize(500,500);

        Clip c = new Clip("res/03_-_Vivaldi_Spring_mvt_3_Allegro_-_John_Harrison_violin.mp3");
        c.start();;
        LineOut lout = LineOut.getInstance();
        while(true){
            lout.writeS(c.read(),c.read());
        }
    }
}
