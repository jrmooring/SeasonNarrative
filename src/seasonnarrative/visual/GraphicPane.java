package seasonnarrative.visual;

import com.jogamp.common.nio.Buffers;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

/**
 * Author: John Mooring (jmooring)
 * Date: 10/12/13
 * Time: 2:46 PM
 */
public class GraphicPane extends GLJPanel implements GLEventListener {


    //vertex shader takes the z component of vPosition and uses it to interpolate between blue and red
    //at the corresponding vetex
    private static final String VERTEX_SHADER =
            "#version 130\n" +
                    "in vec4 vPosition;\n" +
                    "out vec4 vColor;\n" +
                    "\n" +
                    "void main(void) {\n" +
                    "  gl_Position = vec4(vPosition.x, vPosition.y, 0.0, 1.0);\n" +
                    "  vColor = gl_Position;\n" +
                    "}\n";

    private static String FRAGMENT_SHADER =
            "#version 130\n" +
                    "in vec4 vColor;\n" +
                    "out vec4 fColor;\n" +
                    "\n" +
                    "void main(void) {\n" +
                    "  fColor = vec4(1,0,0,1);\n" +
                    "}\n";

    private float vertices[];

    private int vertexShader;
    private int fragmentShader;
    private int shaderProgram;

    private float time = 0, low = 0, med = 0, high = 0, mval = 0;
    private float ilow = 0, imed = 0, ihigh = 0, imval = 0;


    public GraphicPane() {
        super();
        addGLEventListener(this);
        vertices = new float[]{-1, -1, 1, -1, 1, 1, -1, 1};
        try {
            StringBuffer sb = new StringBuffer();
            Scanner sc = new Scanner(new File("res/FragmentShader.glsl"));
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine() + "\n");
            }
            FRAGMENT_SHADER = sb.toString();

        } catch (Exception unused) {
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

        vertexShader = compile(gl, GL3.GL_VERTEX_SHADER, VERTEX_SHADER);
        fragmentShader = compile(gl, GL3.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertexShader);
        gl.glAttachShader(shaderProgram, fragmentShader);
        gl.glLinkProgram(shaderProgram);
    }

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

        float avgC = 2;

        if(!Float.isNaN(low))
        ilow = (ilow*avgC + low)/(avgC + 1);
        if(!Float.isNaN(med))
        imed = (imed*avgC + med)/(avgC + 1);
        if(!Float.isNaN(high))
        ihigh = (ihigh*avgC + high)/(avgC + 1);
        if(!Float.isNaN(mval) && !Float.isInfinite(mval))
        imval = (imval*avgC + mval)/(avgC + 1);

        System.out.println(mval +" , " + imval);

        location = gl.glGetUniformLocation(shaderProgram, "low");
        gl.glUniform1f(location, ilow);
        location = gl.glGetUniformLocation(shaderProgram, "med");
        gl.glUniform1f(location, imed);
        location = gl.glGetUniformLocation(shaderProgram, "high");
        gl.glUniform1f(location, ihigh);
        location = gl.glGetUniformLocation(shaderProgram, "mval");
        gl.glUniform1f(location, imval);

        gl.glDrawArrays(GL3.GL_TRIANGLE_FAN, 0, 6);

        gl.glFlush();

    }

    /**
     * Detaches all shaders and programs
     *
     * @param drawable OpenGL drawable
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2GL3 gl = drawable.getGL().getGL2GL3();
        gl.glDetachShader(shaderProgram, vertexShader);
        gl.glDeleteShader(vertexShader);
        gl.glDetachShader(shaderProgram, fragmentShader);
        gl.glDeleteShader(fragmentShader);
        gl.glDeleteProgram(shaderProgram);
    }

    /**
     * A utility method to create a shader
     *
     * @param gl         The OpenGL context.
     * @param shaderType The type of the shader.
     * @param program    The string containing the program.
     * @return the created shader.
     */
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
            gl.glGetShaderInfoLog(shader, logLength[0], (int[]) null, 0, log, 0);
            System.err.println("Error compiling the shader: " + new String(log));
            System.exit(1);
        }
        return shader;
    }


    /**
     * Overridden as empty method
     */
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i2, int i3, int i4) {
    }

}
