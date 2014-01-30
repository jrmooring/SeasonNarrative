package seasonnarrative.keyframes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

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

	public ArrayList<KeyFrame> KeyFramesList;

	public Keyframes(int parameters) {
		KeyFramesList = new ArrayList<KeyFrame>();
	}

	public void addKeyframe(long timeStamp, float[] parameters) {
		KeyFrame k = new KeyFrame(timeStamp, parameters);
		kk = k;
		if (KeyFramesList.size() == 0) {
			KeyFramesList.add(k);
		} else {
			for (int i = 0; i < KeyFramesList.size(); i++) {
				if (i == KeyFramesList.size() - 1) {
					KeyFramesList.add(k);
					break;
				} else if (KeyFramesList.get(i).timeStamp < timeStamp
						&& KeyFramesList.get(i + 1).timeStamp > timeStamp) {
					KeyFramesList.add(i + 1, k);
					break;
				}
			}
		}
	}

	public float[] getFrame(long timeStamp) {
		KeyFrame k1 = null;
		KeyFrame k2 = null;
		for (int i = 0; i < KeyFramesList.size(); i++) {
			if (KeyFramesList.get(i).timeStamp > timeStamp && i == 0) {
				k2 = KeyFramesList.get(i);
			} else if (KeyFramesList.size() == 1) {
				k1 = KeyFramesList.get(i);
			} else if (KeyFramesList.get(i).timeStamp < timeStamp
					&& KeyFramesList.get(i + 1).timeStamp > timeStamp) {
				k1 = KeyFramesList.get(i);
				k2 = KeyFramesList.get(i + 1);
				break;
			}
		}

		return interpolateLinear(timeStamp, k1, k2);
	}

	private float[] interpolateLinear(long timeStamp, KeyFrame k1, KeyFrame k2) {
		if (k2 == null)
			return k1.parameters;
		if (k1 == null)
			return k2.parameters;

		float[] interpolatedParameters = new float[2]; 
		interpolatedParameters[0] = (int) (((timeStamp - k1.timeStamp)
				* (k2.parameters[0] - k1.parameters[0]) / (k2.timeStamp - k1.timeStamp) + k1.parameters[0]));
		interpolatedParameters[1] = (int) (((timeStamp - k1.timeStamp)
				* (k2.parameters[1] - k1.parameters[1]) /( k2.timeStamp - k1.timeStamp) + k1.parameters[1]));

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
