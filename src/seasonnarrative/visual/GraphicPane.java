package seasonnarrative.visual;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import seasonnarrative.audio.Clip;
import seasonnarrative.audio.LineOut;
import seasonnarrative.keyframes.Keyframes;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Scanner;


public class GraphicPane extends GLJPanel implements GLEventListener, KeyListener, MouseMotionListener {

    private static final String VERTEX_SHADER =
            "#version 130\n" +
                    "in vec4 vPosition;\n" +
                    "out vec4 fPos;\n" +
                    "\n" +
                    "void main(void) {\n" +
                    "  gl_Position = vec4(vPosition.x, vPosition.y, 0.0, 1.0);\n" +
                    "  fPos = gl_Position;\n" +
                    "}\n";

    private static String FRAGMENT_SHADER =
            "#version 130\n" +
                    "in vec4 vColor;\n" +
                    "out vec4 fColor;\n" +
                    "\n" +
                    "void main(void) {\n" +
                    "  fColor = vec4(1,0,0,1);\n" +
                    "}\n";

    private final float vertices[] = new float[]{-1, -1, 1, -1, 1, 1, -1, 1};

    private Keyframes keyFrames;
    private Clip clip;

    private boolean DEBUG = false;
    private int treeSeed = 989;

    private int vertexShader;
    private int fragmentShader;
    private int shaderProgram;

    //FPS counters
    private long gfTime = 0, goTime = 0;

    private Texture tex;
    private Graphics2D g;
    private final BufferedImage img = toCompatibleImage(new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB));

    private float[][] points = new float[50][3];


    private float time = 0, ttime = 0, otime = 0, dt = 0, low = 0, med = 0, high = 0, mval = 0;
    private float bgFuzz = 0, fgFuzz = 0, sqRefract = 0, wind = 0, windDir = 0, windMove = 0, age = 0, fuzzGravity = 1;
    private float fuzzSize = 1;
    private int timeStamp = 0;

    private float len = 50, lenf = 0.86f, offset = 0.0f, spread = 0.9f, widthf = 0.66f;
    private float mouseX = 0, mouseY = 0;

    private Color leafColor = Color.green, trunkColor = Color.black;


    public GraphicPane(Clip c, Keyframes kf) {
        super();
        this.clip = c;
        this.keyFrames = kf;
        addGLEventListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);

        for (int i = 0; i < points.length; i++) {
            points[i][0] = (float) Math.random() * 1024;
            points[i][1] = (float) Math.random() * 1024;
            points[i][2] = (float) Math.random() * 7 + 1;
        }

        try {
            StringBuilder sb = new StringBuilder();
            Scanner sc = new Scanner(new File("res/BackgroundShader.glsl"));
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                sb.append("\n");
            }
            FRAGMENT_SHADER = sb.toString();

        } catch (Exception unused) {
            System.out.println("Couldn't load shader.");
            unused.printStackTrace();
        }

        new Thread() {
            public void run() {
                try {
                    while (true) {
                        try {
                            Thread.sleep(17);
                            timeStamp = (int) (LineOut.SAMPLING_RATE * ((System.currentTimeMillis() - clip.tStart) / 1000f));
                            setParameters(timeStamp);
                            repaint();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                }
            }
        }.start();

        this.setFocusable(true);
        requestFocus();


    }

    private void setParameters(int timeStamp) {
        float[] params = keyFrames.getFrame(timeStamp);
        otime = time;
        time = timeStamp * 0.0001f;
        dt = time - otime;

        age = params[0];
        float windInt = params[1];
        sqRefract = params[2];
        fuzzGravity = params[3];
        fuzzSize = params[4];
        bgFuzz = params[5];
        fgFuzz = params[6];
        trunkColor = new Color((int)params[7], (int)params[8],(int)params[9],(int)params[10]);
        leafColor = new Color((int)params[11], (int)params[12],(int)params[13],(int)params[14]);


        windMove = (windMove * 989 + 10*mouseX + windDir) / 1000;
        wind = windMove * windInt;
        if(wind > 1)
            wind = 1;
        if(wind < -1)
            wind = -1;
        if(Math.random() < 0.01)
            windDir = (float)Math.random()*2-1;
        ttime = time + (float) Math.random() * 0.1f;
       // trunkColor = new Color((int) (127 * Math.sin(time)) + 127, (int) (127 * Math.sin(time + 7)) + 127, (int) (127 * Math.sin(time + 5)) + 127, 255);
    }


    @Override
    public void init(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();

        // set up shaders
        vertexShader = compile(gl, GL3.GL_VERTEX_SHADER, VERTEX_SHADER);
        fragmentShader = compile(gl, GL3.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertexShader);
        gl.glAttachShader(shaderProgram, fragmentShader);
        gl.glLinkProgram(shaderProgram);

        // set up tree drawing
        g = (Graphics2D) img.getGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        try {
            tex = AWTTextureIO.newTexture(gl.getGLProfile(), img, true);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            tex.enable(gl);
            tex.bind(gl);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private BufferedImage toCompatibleImage(BufferedImage image) {
        // obtain the current system graphical settings
        GraphicsConfiguration gfx_config = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        // if image is already compatible and optimized for current system
        // settings, simply return it

        if (image.getColorModel().equals(gfx_config.getColorModel()))
            return image;

        // image is not optimized, so create a new image that is
        BufferedImage new_image = gfx_config.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = (Graphics2D) new_image.getGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return new_image;
    }


    @Override
    public void display(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();

        gfTime = System.currentTimeMillis() - goTime;
        goTime = System.currentTimeMillis();

        gl.glClearColor(0, 0, 0, 1);

        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);

        gl.glUseProgram(shaderProgram);

        IntBuffer intBuffer = Buffers.newDirectIntBuffer(1);
        gl.glGenBuffers(1, intBuffer);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, intBuffer.get(0));

        FloatBuffer floatBuffer = Buffers.newDirectFloatBuffer(vertices);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertices.length * Buffers.SIZEOF_FLOAT, floatBuffer, GL3.GL_STATIC_DRAW);

        int location = gl.glGetAttribLocation(shaderProgram, "vPosition");
        gl.glVertexAttribPointer(location, 2, GL3.GL_FLOAT, false, 0, 0);
        gl.glEnableVertexAttribArray(location);

        location = gl.glGetUniformLocation(shaderProgram, "time");
        gl.glUniform1f(location, time);

        location = gl.glGetUniformLocation(shaderProgram, "bgFuzz");
        gl.glUniform1f(location, bgFuzz);
        location = gl.glGetUniformLocation(shaderProgram, "fgFuzz");
        gl.glUniform1f(location, fgFuzz);
        location = gl.glGetUniformLocation(shaderProgram, "wind");
        gl.glUniform1f(location, wind);
        location = gl.glGetUniformLocation(shaderProgram, "sqRefract");
        gl.glUniform1f(location, sqRefract);


        gl.glActiveTexture(GL3.GL_TEXTURE0);


        g.setColor(new Color(126, 0, 0, 0));
        g.fillRect(0, 0, 1024, 1024);


        Random rand = new Random();
        rand.setSeed(treeSeed);

        // flakes
        g.setColor(Color.white);
        for (float[] point : points) {
            point[1] -= fuzzGravity * 5 * dt * (Math.random() * 0.05 + (point[2] - 1) * 3);
            if (point[1] < 0)
                point[1] = 1024;
            point[0] += 5 * dt * (wind * 10 * (point[2] - 1) + Math.random() * 0.02);
            if (point[0] < 0)
                point[0] = 1024;
            if (point[0] > 1024)
                point[0] = 0;
            g.fillOval((int) point[0], (int) point[1], (int) (point[2]*fuzzSize), (int) (int) (point[2]*fuzzSize));
        }

        // draw the tree with heavy magic number fuckery
        drawTree(g, 0, age, 2, new Point(512, 0), 25 + age * 6.5f, 1.57f, age * 2.3f, wind * -0.2f + (float) Math.sin(ttime * 8.1) * 0.003f * Math.abs(wind), 1.0f + age / 24.0f - Math.abs(wind) * 0.7f + (float) Math.sin(ttime * 8) * 0.03f * Math.abs(wind), trunkColor, rand, age * 2.3f - 4, leafColor);

        tex.updateImage(gl, AWTTextureIO.newTextureData(gl.getGLProfile(), img, true));

        tex.bind(gl);

        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, "tex"), 0);

        gl.glDrawArrays(GL3.GL_TRIANGLE_FAN, 0, 6);

        gl.glFlush();

    }

    public void drawTree(Graphics2D gg, int depth, float maxDepth, int branches, Point start, float len, double angle, float width, float offset, float spread, Color c, Random rand, float leafSize, Color leafColor) {
        gg.setColor(c);
        gg.setStroke(new BasicStroke(width));
        float nlen = len;

        float lsize = leafSize / 2 + rand.nextFloat() * leafSize / 2;
        if (depth - 1 == Math.floor(maxDepth))
            nlen *= maxDepth - Math.floor(maxDepth);

        int red = clamp(leafColor.getRed() + rand.nextInt(30) - 15, 0, 255);

        int green = clamp(leafColor.getGreen() + rand.nextInt(30) - 15, 0, 255);

        int blue = clamp(leafColor.getBlue() + rand.nextInt(30) - 15, 0, 255);

        leafColor = new Color(red, green, blue, leafColor.getAlpha());

        gg.drawLine(start.x, start.y, (int) (start.x + nlen * Math.cos(angle)), (int) (start.y + nlen * Math.sin(angle)));

        if (depth >= maxDepth) {
            gg.setColor(leafColor);
            gg.fillOval((int) (start.x + nlen * (float) Math.cos(angle) - lsize / 2), (int) (start.y + nlen * (float) Math.sin(angle) - lsize / 2), (int) lsize, (int) lsize);
            return;
        }
        for (int i = 0; i < branches; i++) {
            Random nRand = new Random(rand.nextInt());
            len += 5f * (rand.nextFloat() - 0.5);
            if (rand.nextFloat() < 0.95)
                drawTree(gg, depth + 1, maxDepth, branches, new Point((int) (start.x + len * Math.cos(angle + i * 0.05 - 0.025)), (int) (start.y + len * Math.sin(angle + i * 0.05 - 0.025))), len * lenf, offset + angle - spread / 4 + i * (spread / branches) + spread * 0.45 * (rand.nextFloat() - 0.5), width * widthf, offset, spread, c, nRand, leafSize, leafColor);
            else if (maxDepth - depth < 4) {
                gg.setColor(leafColor);
                gg.fillOval((int) (start.x + nlen * (float) Math.cos(angle) - lsize / 2), (int) (start.y + nlen * (float) Math.sin(angle) - lsize / 2), (int) lsize, (int) lsize);
            }
        }
    }

    private int clamp(int i, int i1, int i2) {
        if (i < i1)
            i = i1;
        if (i > i2)
            i = i2;
        return i;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (!DEBUG)
            return;
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRect(5, 5, 200, 200);
        g.setColor(Color.WHITE);
        g.drawString("DEBUG:", 10, 20);
        g.drawString(String.format("FPS: %.1f", 1000.0 / gfTime), 10, 40);
        g.drawString(String.format("wind: %.4f", wind), 10, 60);
        g.drawString(String.format("tree seed: %d", treeSeed), 10, 80);
        g.drawString(String.format("timestamp: %d", timeStamp), 10, 100);
    }


    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();
        gl.glDetachShader(shaderProgram, vertexShader);
        gl.glDeleteShader(vertexShader);
        gl.glDetachShader(shaderProgram, fragmentShader);
        gl.glDeleteShader(fragmentShader);
        gl.glDeleteProgram(shaderProgram);
    }

    private int compile(GL2GL3 gl, int shaderType, String program) {
        int shader = gl.glCreateShader(shaderType);
        String[] lines = new String[]{program};
        int[] lengths = new int[]{lines[0].length()};
        gl.glShaderSource(shader, lines.length, lines, lengths, 0);
        gl.glCompileShader(shader);
        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shader, logLength[0], null, 0, log, 0);
            System.err.println("Error compiling the shader: " + new String(log));
            System.exit(1);
        }
        return shader;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'd')
            DEBUG = !DEBUG;
        if (e.getKeyCode() == KeyEvent.VK_UP)
            ++treeSeed;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)
            --treeSeed;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            clip.tStart += 10 * LineOut.SAMPLING_RATE;
            clip.rr(10);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            clip.tStart -= 10 * LineOut.SAMPLING_RATE;
            if (clip.tStart < 0)
                clip.tStart = 0;
            clip.ff(10);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i2, int i3, int i4) {
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = (((float) e.getX()) / getWidth()) * 2 - 1;
        mouseY = (((float) e.getY()) / getHeight()) * 2 - 1;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
