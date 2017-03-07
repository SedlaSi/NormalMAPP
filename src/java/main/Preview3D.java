package main;




import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.sun.org.apache.xml.internal.utils.res.StringArrayWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Preview3D implements GLEventListener {

    private static GraphicsEnvironment graphicsEnvironment;
    private static boolean isFullScreen = false;
    public static DisplayMode dm, dm_old;
    private static Dimension xgraphic;
    private static Point point = new Point(0,0);

    private GLU glu = new GLU();

    private float rquad = 45.0f;
    //private float rtri = 0.0f;

    private String originalImagePath = "/home/sedlasi1/Desktop/obrazky/coin.jpg";

    private float[] lightAmbient = {0.5f,0.5f,0.5f,1.0f};
    private float[] lightDiffuse = {1.0f,1.0f,1.0f,1.0f};
    private float[] lightPosition = {0.0f,0.0f,2.0f,0.0f};
    private boolean light;

    private int filter = 1;
    private int [] texture = new int[3];

    private String heightPath = "/home/sedlasi1/Desktop/obrazky/stones/height.ppm";
    private String originalPath = "/home/sedlasi1/Desktop/obrazky/small_stones.ppm";
    private String normalPath = "/home/sedlasi1/Desktop/obrazky/stones/normal.ppm";
    private int collumns;
    private int rows;
    private int off;
    private int offNormal;
    private int bodyStart;
    private int bodyStartNormal;
    private int STEP_SIZE = 16;

    private float HEIGHT_RATIO = 1.5f;
    private float scaleValue = 0.05f;

    private byte [] image;
    private byte [] normalMap;
    private int [] heightMap;

    private float heightScale = 0.5f;
    private float [] matrix = new float[]{
            1.0f,0.0f,0.0f,0.0f,
            0.0f,1.0f,0.0f,0.0f,
            0.0f,0.0f,1.0f,0.0f,
            0.0f,0.0f,0.0f,0.0f,
    };


    @Override
    public void display(GLAutoDrawable drawable) {
        // method body
        final GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);     // Clear The Screen And The Depth Buffer

        gl.glUseProgram(resources.program);
        gl.glLoadIdentity();                       // Reset The View
        gl.glUniformMatrix4fv(resources.PVMMatrix, 1, false, FloatBuffer.wrap(matrix));

        //glu.gluLookAt(212,60,194, 186,55,171, 0,1,0);
        //glu.gluLookAt(0,0,-50, 0,0,0, 0,1,0);
        //gl.glScalef(scaleValue,scaleValue*HEIGHT_RATIO,scaleValue);

        gl.glTranslatef(-3.2f,0.2f,-8.0f);
        gl.glRotatef(rquad,1.0f,1.0f,0.0f);

        renderHeightMap(drawable,heightMap);
        gl.glEnd();

        //rquad+=0.2f;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        //method body
        final GL2 gl = drawable.getGL().getGL2();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // method body
        final GL2 gl = drawable.getGL().getGL2();

        //loadShaders(drawable);

        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearColor(0f,0f,0f,0f);
        gl.glClearDepth(1.0);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT,GL2.GL_NICEST);

        gl.glEnable(GL2.GL_TEXTURE_2D);

        try {
            image = Files.readAllBytes(Paths.get(originalPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        heightMap = loadTexture(heightPath);
        initLights(drawable);
        this.light = true;

        try{
            loadGLTextures(drawable);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadGLTextures(GLAutoDrawable drawable) throws IOException {
        final GL2 gl = drawable.getGL().getGL2();
        normalMap = loadNormal(normalPath);
        File im = new File(originalPath);
        //File im = new File(originalImagePath);

        //========
        Texture t = TextureIO.newTexture(im,true);
        texture[0] = t.getTextureObject(gl);

        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_NEAREST);

        gl.glBindTexture(GL2.GL_TEXTURE_2D,texture[0]);

        //=========
        t = TextureIO.newTexture(im,true);
        texture[1] = t.getTextureObject(gl);

        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_LINEAR);

        gl.glBindTexture(GL2.GL_TEXTURE_2D,texture[1]);
        //========
        t = TextureIO.newTexture(im,true);
        texture[2] = t.getTextureObject(gl);

        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_LINEAR_MIPMAP_NEAREST);

        gl.glBindTexture(GL2.GL_TEXTURE_2D,texture[2]);


    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // method body
        final GL2 gl = drawable.getGL().getGL2();
        if(height <= 0){
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0,0,width,height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f,h,1.0,20.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();



    }

    private void initLights(GLAutoDrawable drawable){
        final GL2 gl = drawable.getGL().getGL2();
        gl.glLightfv(GL2.GL_LIGHT1,GL2.GL_AMBIENT,this.lightAmbient,0);
        gl.glLightfv(GL2.GL_LIGHT1,GL2.GL_DIFFUSE,this.lightDiffuse,0);
        gl.glLightfv(GL2.GL_LIGHT1,GL2.GL_POSITION,this.lightPosition,0);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);

    }

    public static void main(String[] args) {

        final GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas glcanvas = new GLCanvas(capabilities);

        Preview3D p = new Preview3D();
        glcanvas.addGLEventListener(p);
        glcanvas.setSize(400,400);

        final FPSAnimator animator = new FPSAnimator(glcanvas,300,true);
        final JFrame frame = new JFrame("Preview");
        frame.getContentPane().add(glcanvas);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if(animator.isStarted()){
                    animator.stop();
                }
                System.exit(0);
            }
        });

        frame.setSize(frame.getContentPane().getPreferredSize());
        graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice [] devices = graphicsEnvironment.getScreenDevices();

        dm_old = devices[0].getDisplayMode();
        dm = dm_old;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int windowX = Math.max(0,(screenSize.width - frame.getWidth())/2);
        int windowY = Math.max(0,(screenSize.height - frame.getHeight())/2);

        frame.setLocation(windowX,windowY);

        frame.setVisible(true);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0,0));
        frame.add(panel,BorderLayout.SOUTH);

        keyBindings(panel,frame,p);
        animator.start();

    }

    private static void keyBindings(JPanel panel,final  JFrame frame,final Preview3D p) {

        ActionMap actionMap = panel.getActionMap();
        InputMap inputMap = panel.getInputMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0),"F1");
        actionMap.put("F1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fullScreen(frame);
            }
        });


    }

    protected static void fullScreen(JFrame f) {
        if(!isFullScreen){
            f.dispose();
            f.setUndecorated(true);
            f.setVisible(true);
            f.setResizable(false);
            xgraphic = f.getSize();
            point = f.getLocation();
            f.setLocation(0,0);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            f.setSize((int)screenSize.getWidth(),(int)screenSize.getHeight());
            isFullScreen = true;
        } else {
            f.dispose();
            f.setUndecorated(false);
            f.setResizable(true);
            f.setLocation(point);
            f.setSize(xgraphic);
            f.setVisible(true);
            isFullScreen = false;

        }
    }

    private int [] loadTexture(String path){ // vraci pouze pole s hodnotami 0 - 255 int bez hlavicky!!! size = collumns * rows
        byte [] fr;
        int [] out;
        try {
            fr =  Files.readAllBytes(Paths.get(path));

            int i = 3;
            StringBuilder stb = new StringBuilder();
            while(true){
                if(fr[i] == '#'){
                    i++;
                    while(fr[i] != '\n') i++;
                    while(fr[i] == '\n') i++;
                } else break;

            }
            off = i;
            while(fr[off] != 10 && fr[off] != ' ') off++;
            while(i < off){
                stb.append((char)fr[i]);
                i++;
            }
            collumns = Integer.parseInt(stb.toString());

            off++;
            i = off;
            while(fr[off] != 10 && fr[off] != ' ') off++;
            stb = new StringBuilder();
            while(i < off){
                stb.append((char)fr[i]);
                i++;
            }
            rows = Integer.parseInt(stb.toString());
            //System.out.println("collumns: "+collumns+" rows: "+rows);
            out = new int [collumns*rows];
            off += 5;
            bodyStart = off;
            int val;
            for(i = 0; i < out.length; i++){
                val = (int)(fr[i*3 + off] & 0xFF) - 60;

                if(val < 0) val = 0;
                else if (val > 255) val = 255;
                out[i] = (int)val;
                //System.out.println(out[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return out;
    }

    private byte [] loadNormal(String path){ // vraci pouze pole s hodnotami 0 - 255 int bez hlavicky!!! size = collumns * rows
        byte [] fr;
        try {
            fr =  Files.readAllBytes(Paths.get(path));

            int i = 3;
            StringBuilder stb = new StringBuilder();
            while(true){
                if(fr[i] == '#'){
                    i++;
                    while(fr[i] != '\n') i++;
                    while(fr[i] == '\n') i++;
                } else break;

            }
            off = i;
            while(fr[off] != 10 && fr[off] != ' ') off++;
            while(i < off){
                stb.append((char)fr[i]);
                i++;
            }
            collumns = Integer.parseInt(stb.toString());

            off++;
            i = off;
            while(fr[off] != 10 && fr[off] != ' ') off++;
            stb = new StringBuilder();
            while(i < off){
                stb.append((char)fr[i]);
                i++;
            }
            rows = Integer.parseInt(stb.toString());
            //System.out.println("collumns: "+collumns+" rows: "+rows);
            offNormal += 5;
            bodyStartNormal = off;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fr;
    }


    private void renderHeightMap(GLAutoDrawable drawable,int [] fr){
        final GL2 gl = drawable.getGL().getGL2();
        int X = 0, Y = 0; // for field loop
        int x,y,z;
        //gl.glBegin(GL2.GL_QUADS);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glBindTexture(GL2.GL_TEXTURE_2D,texture[0]);
        //gl.glBegin(GL2.GL_LINES);

        for(X = 0; X+STEP_SIZE < collumns; X += STEP_SIZE){
            for(Y = 0; Y+STEP_SIZE < rows; Y += STEP_SIZE){
                // Souřadnice levého dolního vertexu
                if(((Y+STEP_SIZE)*collumns + X + STEP_SIZE) >= collumns*rows){
                    break;
                }
                x = X;
                y = (int)((float)fr[Y*collumns + X]*heightScale);
                z = Y;

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice levého horního vertexu
                x = X;
                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X]*heightScale);
                z = Y + STEP_SIZE ;

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice pravého horního vertexu

                x = X + STEP_SIZE;

                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X + STEP_SIZE]*heightScale);

                z = Y + STEP_SIZE ;

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));


                // Souřadnice levého dolního vertexu
                if(((Y+STEP_SIZE)*collumns + X + STEP_SIZE) >= collumns*rows){
                    break;
                }
                x = X;
                y = (int)((float)fr[Y*collumns + X]*heightScale);
                z = Y;

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice pravého horního vertexu

                x = X + STEP_SIZE;

                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X + STEP_SIZE]*heightScale);

                z = Y + STEP_SIZE ;

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));


                // Souřadnice pravého dolního vertexu
                x = X + STEP_SIZE;
                y = (int)((float)fr[Y*collumns + X + STEP_SIZE]*heightScale);
                z = Y;
                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

            }
        }

    }

    private class Resources{
        int program;
        int vbo_positions, vbo_indices;
        int vao;
        int position;
        int PVMMatrix;
    }

    private Resources resources = new Resources();

    private String vertexProgram =
            "#version 130\n" +
                    "\n" +
                    "uniform mat4  mPVM;" +
                    "" +
                    "void main(){" +
                    "gl_Position = gl_ModelViewProjectionMatrix *gl_Vertex;" +
                    "}";

    private String fragmentProgram =
            "#version 130\n" +
                    "\n" +
                    "uniform mat4  mPVM;" +
                    "uniform sampler2D texture;" +
                    "void main(){" +
                    "vec4 tex = texture2D ( texture, uvVarying );" +
                    "gl_FragColor = vec4(0.0f,0.0f,0.0f,1.0f);" +
                    "}";

    private void loadShaders(GLAutoDrawable drawable){
        final GL2 gl = drawable.getGL().getGL2();

        // vertex shader
        int vertShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
        if(vertShader == 0){
            System.out.println("Cannot create VS");
        }
        gl.glShaderSource(vertShader,1, new String[]{vertexProgram},null);
        gl.glCompileShader(vertShader);
        IntBuffer compilationResult = IntBuffer.wrap(new int[1]);
        gl.glGetShaderiv(vertShader,GL2.GL_COMPILE_STATUS,compilationResult);
        if(compilationResult.get(0) == GL2.GL_FALSE){
            System.out.println("Compilation VS failed.");
            byte[] infoLog = new byte[1024];
            gl.glGetShaderInfoLog(vertShader, 1024, null, 0, infoLog, 0);
            gl.glDeleteShader(vertShader);
            System.out.println("Vertex shader compilation failed with: " + new String(infoLog));
            System.exit(100);
        }

        // fragment shader
        int fragShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
        if(fragShader == 0){
            System.out.println("Cannot create FS");
        }
        gl.glShaderSource(fragShader,1, new String[]{fragmentProgram},null);
        gl.glCompileShader(fragShader);
        compilationResult = IntBuffer.wrap(new int[1]);
        gl.glGetShaderiv(fragShader,GL2.GL_COMPILE_STATUS,compilationResult);
        if(compilationResult.get(0) == GL2.GL_FALSE){
            System.out.println("Compilation FS failed.");
            byte[] infoLog = new byte[1024];
            gl.glGetShaderInfoLog(fragShader, 1024, null, 0, infoLog, 0);
            gl.glDeleteShader(fragShader);
            System.out.println("Fragment shader compilation failed with: " + new String(infoLog));
            System.exit(100);
        }

        int programHandle = gl.glCreateProgram();
        if(programHandle == 0){
            System.out.println("Cannot create program handle.");
        }

        gl.glAttachShader(programHandle,vertShader);
        gl.glAttachShader(programHandle,fragShader);
        gl.glLinkProgram(programHandle);

        compilationResult = IntBuffer.wrap(new int[1]);
        gl.glGetProgramiv(programHandle,GL2.GL_LINK_STATUS,compilationResult);
        if(compilationResult.get(0) == GL2.GL_FALSE){
            System.out.println("Cannot compile program.");
        } else {

            gl.glUseProgram(programHandle);

        }

        resources.program = programHandle;

        // in attr
        resources.position = gl.glGetAttribLocation(resources.program, "position");

        // uniform
        resources.PVMMatrix = gl.glGetUniformLocation(resources.program, "mPVM");
        texture[0] = gl.glGetUniformLocation(resources.program, "texture");




    }

    private void connectVertexAttributes(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glBindVertexArray(resources.vao);

        // vertex positions
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, resources.vbo_positions);
        gl.glEnableVertexAttribArray(resources.position);
        //gl.glVertexAttribPointer(resources.position, 3, GL2.GL_FLOAT, GL2.GL_FALSE, , 0);  // [xyz][nx,ny,nz][s,t]

        // triangle indices
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, resources.vbo_indices);

        gl.glBindVertexArray(0);
        //CHECK_GL_ERROR();
    }


}