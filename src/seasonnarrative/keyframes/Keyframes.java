package seasonnarrative.keyframes;

/**
 * Created by John Mooring (jmooring)
 * on 1/27/14 11:45 PM.
 *
 * Keyframes have timestamp, and parameters associated with them.
 * Keyframing should take a timestamp, and linearly interpolate between parameters
 * in adjascent keyframes.
 *
 * Ex:
 * Keyframe1(time = 00, branchLength = 05, color1R = 123)
 * Keyframe2(time = 10, branchLength = 10, color1R = 100)
 * Keyframe3(time = 20, branchLength = 30, color1R = 200)
 * Keyframe4(time = 24, branchLength = 33, color1R = 140)
 *
 * query time = 15 should return
 * Keyframe(time = 15, branchLength = 20, color1R = 150)
 */
public class Keyframes {

    private class KeyFrame {
        public long timeStamp;
        public int[] parameters;

        public KeyFrame(long timeStamp, int[] parameters) {
            this.timeStamp = timeStamp;
            this.parameters = parameters;
        }
    }

    //TODO: choose appropriate datastructure

    public Keyframes(int parameters) {
        //TODO: initialize datastructure
    }

    public void addKeyframe(long timeStamp, int[] parameters) {
        KeyFrame k = new KeyFrame(timeStamp, parameters);
        //TODO: add k to datastructure
    }

    public int[] getFrame(long timeStamp) {
        KeyFrame k1 = null; //TODO: retrieve k1 from datastructure
        KeyFrame k2 = null; //TODO: retrieve k2 from datastructure

        return interpolateLinear(timeStamp, k1, k2);
    }

    private int[] interpolateLinear(long timeStamp, KeyFrame k1, KeyFrame k2) {
        if (k2 == null)
            return k1.parameters;
        if (k1 == null)
            return k2.parameters;

        int[] interpolatedParameters = null; //TODO: interpolate linearly

        return interpolatedParameters;
    }
}
