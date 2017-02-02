package seasonnarrative

import seasonnarrative.audio.Clip
import seasonnarrative.audio.LineOut
import seasonnarrative.keyframes.Keyframes
import seasonnarrative.visual.GraphicPane

import javax.swing.*
import java.io.File

object Main {

    @Throws(Exception::class)
    @JvmStatic fun main(args: Array<String>) {
        val gui = JFrame("Season Narrative")
        Thread.sleep(1000)

        val kf = Keyframes()
        kf.followFile(File("res/timeStamps"))

        val c = Clip("res/03_-_Vivaldi_Spring_mvt_3_Allegro_-_John_Harrison_violin.mp3")
        c.start()
        val volume = 1.0
        val lout = LineOut.getInstance()
        object : Thread() {
            override fun run() {
                while (true) {
                    lout.writeS(c.read() * volume, c.read() * volume)
                }
            }
        }.start()


        gui.add(GraphicPane(c, kf))
        gui.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        gui.isVisible = true
        gui.setSize(500, 500)
    }
}
