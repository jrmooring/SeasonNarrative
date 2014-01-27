package seasonnarrative.visual;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Scanner;


public class GraphicPane extends GLJPanel implements GLEventListener {

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

    private static final boolean DEBUG = true;

    private int vertexShader;
    private int fragmentShader;
    private int shaderProgram;

    private Texture tex;
    private Graphics g;
    private BufferedImage img;


    private float time = 0, low = 0, med = 0, high = 0, mval = 0;
    private float ilow = 0, imed = 0, ihigh = 0, imval = 0;

    private float len = 50, lenf = 0.88f, offset = 0.0f, spread = 0.9f, widthf = 0.66f;


    public GraphicPane() {
        super();
        addGLEventListener(this);

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
                        Thread.sleep(17);
                        repaint();
                    }
                } catch (Exception e) {
                }
            }
        }.start();


    }




    @Override
    public void init(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();

        //set up shaders
        vertexShader = compile(gl, GL3.GL_VERTEX_SHADER, VERTEX_SHADER);
        fragmentShader = compile(gl, GL3.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertexShader);
        gl.glAttachShader(shaderProgram, fragmentShader);
        gl.glLinkProgram(shaderProgram);

        //Set up tree drawing
        img = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);
        g = img.getGraphics();

        try{
            tex = AWTTextureIO.newTexture(gl.getGLProfile(), img, true);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            tex.setTexParameteri(gl, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
            tex.enable(gl);
            tex.bind(gl);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private int x = 0;

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();

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
        gl.glUniform1f(location, time += 0.1);


        location = gl.glGetUniformLocation(shaderProgram, "low");
        gl.glUniform1f(location, ilow);
        location = gl.glGetUniformLocation(shaderProgram, "med");
        gl.glUniform1f(location, imed);
        location = gl.glGetUniformLocation(shaderProgram, "high");
        gl.glUniform1f(location, ihigh);
        location = gl.glGetUniformLocation(shaderProgram, "mval");
        gl.glUniform1f(location, imval);


        gl.glActiveTexture(GL3.GL_TEXTURE0);

        //draw tree
        //       .< this value controls frequency of cpu rendering
        if(x++ % 2 == 0){
            Graphics2D gg = (Graphics2D) g;
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
            gg.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            gg.setColor(new Color(126, 0, 0, 0));
            gg.fillRect(0, 0, 1024, 1024);

            Random rand = new Random();
            rand.setSeed(1000);
            drawTree(gg, 0,(float)( Math.sin(time) + 1) * 6 , 2, new Point(512, 0), 40 + (float)( Math.sin(time) + 1)*50, 1.57, (float)( Math.sin(time) + 1)*10, (float) Math.sin(time * 0.1) * 0.2f, 0.9f + (float) Math.sin(time * 0.2) * 0.7f, new Color((int)(127*Math.sin(time))+127,(int)(127*Math.sin(time+7))+127,(int)(127*Math.sin(time+5))+127,255), rand);

            tex.updateImage(gl,AWTTextureIO.newTextureData(gl.getGLProfile(), img, true));
        }

        tex.bind(gl);

        gl.glUniform1i(gl.glGetUniformLocation(shaderProgram,"tex"), 0);

        gl.glDrawArrays(GL3.GL_TRIANGLE_FAN, 0, 6);

        gl.glFlush();

    }

    public void setTree(float height, float branchLength, float width, float offset, float spread){}

    public void drawTree(Graphics2D gg, int depth, float maxDepth, int branches, Point start, double len, double angle, float width, float offset, float spread, Color c, Random rand){
        gg.setColor(c);
        gg.setStroke(new BasicStroke(width));
        double nlen = len;
        if(depth  == Math.floor(maxDepth))
            nlen *= maxDepth - Math.floor(maxDepth);
        gg.draw(new Line2D.Float(start.x, start.y, (int) (start.x + nlen * Math.cos(angle)), (int) (start.y + nlen * Math.sin(angle))));
        if(depth + 1 >= maxDepth)
            return;
        for(int i = 0; i < branches; i++){
            Random nRand = new Random(rand.nextInt());
            len += 5f*(rand.nextFloat() - 0.5);
            drawTree(gg, depth + 1, maxDepth, branches, new Point((int) (start.x + len * Math.cos(angle)), (int) (start.y + len * Math.sin(angle))), len * lenf, offset + angle - spread / 4 + i * (spread / branches) + 0.4 * (rand.nextFloat() - 0.5), width * widthf, offset, spread, c, nRand);
        }
    }

    public void paint(Graphics g){
        super.paint(g);
        if(!DEBUG)
            return;
        g.setColor(new Color(0,0,0,80));
        g.fillRect(5,5,200,200);
        g.setColor(Color.WHITE);
        g.drawString("DEBUG:",10,20);
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
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i2, int i3, int i4) {
    }

}
