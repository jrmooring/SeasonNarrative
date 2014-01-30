package seasonnarrative;

import seasonnarrative.audio.Clip;
import seasonnarrative.audio.LineOut;
import seasonnarrative.keyframes.Keyframes;
import seasonnarrative.visual.GraphicPane;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception{
        JFrame gui = new JFrame("Season Narrative");
Thread.sleep(1000);

        Keyframes kf = new Keyframes();
        kf.followFile(new File("res/timeStamps"));

        final Clip c = new Clip("res/03_-_Vivaldi_Spring_mvt_3_Allegro_-_John_Harrison_violin.mp3");
        c.start();
        final double volume = 1.0;
        final LineOut lout = LineOut.getInstance();
        new Thread() {
            public void run() {
                while (true) {
                    lout.writeS(c.read()*volume, c.read()*volume);
                }
            }
        }.start();


        gui.add(new GraphicPane(c,kf));
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.setVisible(true);
        gui.setSize(500, 500);
    }
}
