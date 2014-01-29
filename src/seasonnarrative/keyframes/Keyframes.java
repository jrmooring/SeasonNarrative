package seasonnarrative.keyframes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by John Mooring (jmooring)
 * on 1/27/14 11:45 PM.
 * <p/>
 * Keyframes have timestamp, and parameters associated with them.
 * Keyframing should take a timestamp, and linearly interpolate between parameters
 * in adjascent keyframes.
 * <p/>
 * Ex:
 * Keyframe1(time = 00, branchLength = 05, color1R = 123)
 * Keyframe2(time = 10, branchLength = 10, color1R = 100)
 * Keyframe3(time = 20, branchLength = 30, color1R = 200)
 * Keyframe4(time = 24, branchLength = 33, color1R = 140)
 * <p/>
 * query time = 15 should return
 * Keyframe(time = 15, branchLength = 20, color1R = 150)
 */
public class Keyframes {

    private class KeyFrame {
        public long timeStamp;
        public float[] parameters;

        public KeyFrame(long timeStamp, float[] parameters) {
            this.timeStamp = timeStamp;
            this.parameters = parameters;
        }
    }

    KeyFrame kk = null;

    //TODO: choose appropriate datastructure

    public Keyframes() {
        //TODO: initialize datastructure
    }

    public void addKeyframe(long timeStamp, float[] parameters) {
        KeyFrame k = new KeyFrame(timeStamp, parameters);
        kk = k;
        //TODO: add k to datastructure
    }

    public float[] getFrame(long timeStamp) {
        KeyFrame k1 = this.kk; //TODO: retrieve k1 from datastructure
        KeyFrame k2 = null; //TODO: retrieve k2 from datastructure

        return interpolateLinear(timeStamp, k1, k2);
    }

    private float[] interpolateLinear(long timeStamp, KeyFrame k1, KeyFrame k2) {
        if (k2 == null && k1 != null)
            return k1.parameters;
        if (k1 == null && k2 != null)
            return k2.parameters;

        float[] interpolatedParameters = null; //TODO: interpolate linearly

        return interpolatedParameters; 
    }

    public void clear() {
    }

    public void load(File f) {
        clear();
        try {
            Scanner sc = new Scanner(f);
            while(sc.hasNextLine()){
                String s = sc.nextLine();
                if(s.trim().startsWith("#"))
                    continue;
                s += "," + sc.nextLine();
                String[] vals = s.replace("(", "").replace(")", "").split(",");
                float[] params = new float[vals.length - 1];
                for(int i = 1; i < vals.length; i++){
                    params[i-1] = Float.parseFloat(vals[i]);
                }
                addKeyframe(Long.parseLong(vals[0].trim()),params);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void followFile(final File file){
        clear();
        load(file);
        Thread fileWatcher = new Thread() {
            public void run() {
                while (true) {
                    try{
                    String prevContents = null;
                    String newContents = null;
                    File oldFile = null;
                    if (file != null) {
                        prevContents = getFileContents(file);
                        oldFile = file;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                    if (file != null && (newContents = getFileContents(file)) != null
                            && prevContents != null
                            && !newContents.equals(prevContents)
                            && oldFile.equals(file)) {
                       clear();
                        load(file);
                    }
                    }catch(Exception ignore){}
                }
            }
        };
        fileWatcher.start();
    }

    public String getFileContents(File f){
        StringBuilder sb = new StringBuilder();
        try {
            Scanner sc = new Scanner(f);
            while(sc.hasNextLine())
                sb.append(sc.nextLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
