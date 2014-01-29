package seasonnarrative.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Author: John Mooring (jmooring)
 * Date: 10/12/13
 * Time: 5:49 PM
 */

public class Clip{
    private double[] audio;
    private boolean playing = false;
    private int loaded = 0;
    private int location = 0;

    public long tStart = -1;



    public Clip(final String f) {
        new Thread() {
            public void run() {
                AudioInputStream din = null;
                try {
                    File file = new File(f);
                    AudioInputStream in = AudioSystem.getAudioInputStream(file);
                    AudioFormat baseFormat = in.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                            baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                            false);
                    audio = new double[AudioSystem.getAudioFileFormat(file)
                            .getByteLength() * 8];
                    din = AudioSystem.getAudioInputStream(decodedFormat, in);

                    byte[] data = new byte[4096];

                    while (din.read(data, 0, data.length) != -1) {
                        ByteBuffer bf = ByteBuffer.wrap(data);
                        bf.order(ByteOrder.LITTLE_ENDIAN);

                        while (bf.hasRemaining())
                            audio[loaded++] = (bf.getShort() / ((double) Short.MAX_VALUE));
                    }
                    din.close();


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (din != null) {
                        try {
                            din.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        }.start();
    }

    public void start() {
        location = 0;
        playing = true;
    }

    public void stop() {
        playing = false;
        location = 0;
    }

    public double read() {
        if(tStart < 0)
            tStart = System.currentTimeMillis();
        if (playing) {
            try {
                while(location >= loaded){
                    Thread.sleep(10);
                }
                if (location++ >= audio.length - 1) {
                    stop();
                    location = 0;
                }
                return audio[location];
            } catch (Exception e) {
                return 0;
            }
        } else
            return 0;
    }


    public void rr(float f) {
        location -= f * LineOut.SAMPLING_RATE;
    }

    public void ff(float f) {
        location += f * LineOut.SAMPLING_RATE;
    }
}