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

public class Preview implements GLEventListener {

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
    private int STEP_SIZE = 2;

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

    /**
     *
     * new
     */
    // Handle for our shader program
    int	            r3dShaderProgram;

    // Locations of our UNIFORM variables.
    //Type needs to be INT not UINT because if the location of the uniform variable cannot be found glGetUniformLocation returns -1!
    int               locMVMatrix;
    int               locMVPMatrix;
    int               locNormalMatrix;
    int               locLightPos;
    int               locAmbientColour;
    int               locDiffuseColour;
    int               locSpecularColour;
    int               locColourMap; // The texture map
    int               locNormalMap; // The normal  map

    // Handles for our textures
    int              textureID;
    int              normalMapID;


    //GlGeometryTransform transformPipeline = new GlGeometryTransform();

    float vEyeLightPos[]    = { -20.0f, 20.0f, 100.0f };
    float vAmbientColour[]  = { 0.1f, 0.1f, 0.1f, 1.0f };
    float vDiffuseColour[]  = { 1.0f, 1.0f, 1.0f, 1.0f };
    float vSpecularColour[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    /**
     *
     *
     *
     */

    @Override
    public void display(GLAutoDrawable drawable) {
        // method body
        final GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);     // Clear The Screen And The Depth Buffer

        //gl.glUseProgram(resources.program);
        gl.glLoadIdentity();                       // Reset The View
        //gl.glUniformMatrix4fv(resources.PVMMatrix, 1, false, FloatBuffer.wrap(matrix));

        glu.gluLookAt(212,60,194, 186,55,171, 0,1,0);
        //gl.glScalef(scaleValue,scaleValue*HEIGHT_RATIO,scaleValue);
        gl.glEnable(GL2.GL_LIGHTING);

        gl.glTranslatef(-3.2f,0.2f,-8.0f);
        gl.glRotatef(rquad,1.0f,1.0f,0.0f);

        /**
         *
         */
        gl.glActiveTexture(GL2.GL_TEXTURE0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);

        gl.glActiveTexture(GL2.GL_TEXTURE1);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, normalMapID);

        gl.glUseProgram(r3dShaderProgram); // Specify our shader program

        // Specify all the uniform variables for our shader program
        gl.glUniform4fv(locAmbientColour,      1, FloatBuffer.wrap(vAmbientColour));
        gl.glUniform4fv(locDiffuseColour,      1, FloatBuffer.wrap(vDiffuseColour));
        gl.glUniform4fv(locSpecularColour,     1, FloatBuffer.wrap(vSpecularColour));

        gl.glUniform1i(locColourMap, 0); // Specify that our texture (called "colourMap" in the shader, is on texture unit 0)
        gl.glUniform1i(locNormalMap, 1); // Specify that our texture (called "normalMap" in the shader, is on texture unit 1)

        gl.glUniform3fv(locLightPos,           1, FloatBuffer.wrap(vEyeLightPos));
        /*gl.glUniformMatrix4fv(locMVMatrix,     1, false, FloatBuffer.wrap(transformPipeline.getModelViewMatrix()));
        gl.glUniformMatrix4fv(locMVPMatrix,    1, false, FloatBuffer.wrap(transformPipeline.getModelViewProjectionMatrix()));
        gl.glUniformMatrix3fv(locNormalMatrix, 1, false, FloatBuffer.wrap(transformPipeline.getNormalMatrix(true)));*/

        /**
         *
         */




        renderHeightMap(drawable,heightMap);
        gl.glEnd();

        rquad+=0.2f;
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

        loadShaders(drawable);

        /**
         *
         */
        File im = new File(originalPath);
        //File im = new File(originalImagePath);

        //========
        Texture t = null;
        try {
            t = TextureIO.newTexture(im,true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        textureID = t.getTextureObject(gl);
        gl.glGenTextures(1,IntBuffer.wrap(new int[textureID]));
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_NEAREST);
        gl.glBindTexture(GL2.GL_TEXTURE_2D,textureID);

        // Load the texture for the torus

        t = null;
        im = new File(normalPath);
        try {
            t = TextureIO.newTexture(im,true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        gl.glActiveTexture(GL2.GL_TEXTURE1);
        normalMapID = t.getTextureObject(gl);
        gl.glGenTextures(1,IntBuffer.wrap(new int[normalMapID]));
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MAG_FILTER,GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D,GL2.GL_TEXTURE_MIN_FILTER,GL2.GL_NEAREST);
        gl.glBindTexture(GL2.GL_TEXTURE_2D,textureID);

        // Load shaders and bind attributes
        r3dShaderProgram = resources.program;

        locMVMatrix = gl.glGetUniformLocation(r3dShaderProgram, "mvMatrix");

        locMVPMatrix = gl.glGetUniformLocation(r3dShaderProgram, "mvpMatrix");

        locNormalMatrix = gl.glGetUniformLocation(r3dShaderProgram, "normalMatrix");

        locLightPos = gl.glGetUniformLocation(r3dShaderProgram, "vLightPosition");

        locAmbientColour = gl.glGetUniformLocation(r3dShaderProgram, "ambientColour");

        locDiffuseColour = gl.glGetUniformLocation(r3dShaderProgram, "diffuseColour");

        locSpecularColour = gl.glGetUniformLocation(r3dShaderProgram, "specularColour");

        locColourMap = gl.glGetUniformLocation(r3dShaderProgram, "colourMap"); // The texture map

        locNormalMap = gl.glGetUniformLocation(r3dShaderProgram, "normalMap"); // The normal map

        /**
         *
         */




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


        // Create the projection matrix, and load it on to the projection matrix stack
        /*viewFrustum.SetPerspective(fieldOfView, float(windowWidth)/float(windowHeight), zNear, zFar);

        projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix()); // Get the projection matrix from our frustum (defined in the line above) and load it into our proper projection matrix stack

        // Set the transformation pipeline to use the two matrix stacks
        transformPipeline.setMatrixStacks(modelViewMatrix, projectionMatrix);*/


        /*try{
            loadGLTextures(drawable);
        } catch (Exception e){
            e.printStackTrace();
        }*/

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

        Preview p = new Preview();
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

    private static void keyBindings(JPanel panel,final  JFrame frame,final Preview p) {

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
                //gl.glColor3f(0,0,(float)((float)(y)/256.0f)-0.15f);
                //gl.glColor3b(image[3*Y*collumns + 3*X],image[3*Y*collumns + 3*X+1],image[3*Y*collumns + 3*X+2]);

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                //gl.glNormal3b();
                //gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice levého horního vertexu
                x = X;
                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X]*heightScale);
                z = Y + STEP_SIZE ;

                //gl.glColor3f(0,0,(float)((float)(y)/255.0));
                //gl.glColor3b(image[3*(Y+STEP_SIZE)*collumns + 3*X+off],image[3*(Y+STEP_SIZE)*collumns + 3*X+1+off],image[3*(Y+STEP_SIZE)*collumns + 3*X+2+off]);
                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                //gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice pravého horního vertexu

                x = X + STEP_SIZE;

                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X + STEP_SIZE]*heightScale);

                z = Y + STEP_SIZE ;

                //gl.glColor3f(0,0,(float)((float)(y)/255.0));
                //gl.glColor3b(image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+off],image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+1+off],image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+2+off]);
                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                // /gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));


                // Souřadnice levého dolního vertexu
                if(((Y+STEP_SIZE)*collumns + X + STEP_SIZE) >= collumns*rows){
                    break;
                }
                x = X;
                y = (int)((float)fr[Y*collumns + X]*heightScale);
                z = Y;
                //gl.glColor3f(0,0,(float)((float)(y)/256.0f)-0.15f);
                //gl.glColor3b(image[3*Y*collumns + 3*X],image[3*Y*collumns + 3*X+1],image[3*Y*collumns + 3*X+2]);

                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                //gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));

                // Souřadnice pravého horního vertexu

                x = X + STEP_SIZE;

                y = (int)((float)fr[(Y+STEP_SIZE)*collumns + X + STEP_SIZE]*heightScale);

                z = Y + STEP_SIZE ;

                //gl.glColor3f(0,0,(float)((float)(y)/255.0));
                //gl.glColor3b(image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+off],image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+1+off],image[3*(Y+STEP_SIZE)*collumns + 3*(X+STEP_SIZE)+2+off]);
                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                // /gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
                gl.glVertex3f((float)(x/255.0),(float)(y/255.0),(float)(z/255.0));


                // Souřadnice pravého dolního vertexu
                x = X + STEP_SIZE;
                y = (int)((float)fr[Y*collumns + X + STEP_SIZE]*heightScale);
                z = Y;
                //gl.glColor3f(0,0,(float)((float)(y)/255.0));
                //gl.glColor3b(image[3*(Y)*collumns + 3*(X+STEP_SIZE)+off],image[3*(Y)*collumns + 3*(X+STEP_SIZE)+1+off],image[3*(Y)*collumns + 3*(X+STEP_SIZE)+2+off]);
                gl.glTexCoord2f(((float)x/(float)collumns),((float)z/(float)rows));
                //gl.glVertex3i(x, y, z);// Definování vertexu
                //gl.glNormal3b(normalMap[3*z*collumns + 3*x + bodyStartNormal],normalMap[3*z*collumns + 3*x + bodyStartNormal+1],normalMap[3*z*collumns + 3*x + bodyStartNormal+2]);
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
                    "// Incoming per-vertex attribute values\n" +
                    "in vec4 vVertex;\n" +
                    "in vec3 vNormal;\n" +
                    "in vec4 vTexture;\n" +
                    " \n" +
                    "uniform mat4 mvpMatrix;\n" +
                    "uniform mat4 mvMatrix;\n" +
                    "uniform mat3 normalMatrix;\n" +
                    "uniform vec3 vLightPosition;\n" +
                    " \n" +
                    "// Outgoing normal and light direction to fragment shader\n" +
                    "smooth out vec3 vVaryingNormal;\n" +
                    "smooth out vec3 vVaryingLightDir;\n" +
                    "smooth out vec2 vTexCoords;\n" +
                    " \n" +
                    "void main(void) \n" +
                    "{\n" +
                    " \n" +
                    "    // Get surface normal in eye coordinates and pass them through to the fragment shader\n" +
                    "    vVaryingNormal = normalMatrix * vNormal;\n" +
                    " \n" +
                    "    // Get vertex position in eye coordinates\n" +
                    "    vec4 vPosition4 = mvMatrix * vVertex;\n" +
                    "    vec3 vPosition3 = vPosition4.xyz / vPosition4.w;\n" +
                    " \n" +
                    "    // Get vector to light source\n" +
                    "    vVaryingLightDir = normalize(vLightPosition - vPosition3);\n" +
                    " \n" +
                    "    // Pass the texture coordinates through the vertex shader so they get smoothly interpolated\n" +
                    "    vTexCoords = vTexture.st;\n" +
                    " \n" +
                    "    // Transform the geometry through the modelview-projection matrix\n" +
                    "    gl_Position = mvpMatrix * vVertex;\n" +
                    "}";

    private String fragmentProgram =
            "#version 130\n" +
                    "\n" +
                    "// Uniforms\n" +
                    "uniform vec4 ambientColour;\n" +
                    "uniform vec4 diffuseColour;\n" +
                    "uniform vec4 specularColour;\n" +
                    "uniform sampler2D colourMap; // This is the original texture\n" +
                    "uniform sampler2D normalMap; // This is the normal-mapped version of our texture\n" +
                    " \n" +
                    "// Input from our vertex shader\n" +
                    "smooth in vec3 vVaryingNormal;\n" +
                    "smooth in vec3 vVaryingLightDir;\n" +
                    "smooth in vec2 vTexCoords;\n" +
                    " \n" +
                    "// Output fragments\n" +
                    "out vec4 vFragColour;\n" +
                    " \n" +
                    "void main(void)\n" +
                    "{ \n" +
                    "\tconst float maxVariance = 2.0; // Mess around with this value to increase/decrease normal perturbation\n" +
                    "\tconst float minVariance = maxVariance / 2.0;\n" +
                    " \n" +
                    "\t// Create a normal which is our standard normal + the normal map perturbation (which is going to be either positive or negative)\n" +
                    "\tvec3 normalAdjusted = vVaryingNormal + normalize(texture2D(normalMap, vTexCoords.st).rgb * maxVariance - minVariance);\n" +
                    " \n" +
                    "\t// Calculate diffuse intensity\n" +
                    "\tfloat diffuseIntensity = max(0.0, dot(normalize(normalAdjusted), normalize(vVaryingLightDir)));\n" +
                    " \n" +
                    "\t// Add the diffuse contribution blended with the standard texture lookup and add in the ambient light on top\n" +
                    "\tvec3 colour = (diffuseIntensity * diffuseColour.rgb) * texture2D(colourMap, vTexCoords.st).rgb + ambientColour.rgb;\n" +
                    " \n" +
                    "\t// Set the almost final output color as a vec4 - only specular to go!\n" +
                    "\tvFragColour = vec4(colour, 1.0);\n" +
                    " \n" +
                    "\t// Calc and apply specular contribution\n" +
                    "\tvec3 vReflection        = normalize(reflect(-normalize(normalAdjusted), normalize(vVaryingLightDir)));\n" +
                    "\tfloat specularIntensity = max(0.0, dot(normalize(normalAdjusted), vReflection));\n" +
                    " \n" +
                    "\t// If the diffuse light intensity is over a given value, then add the specular component\n" +
                    "\t// Only calc the pow function when the diffuseIntensity is high (adding specular for high diffuse intensities only runs faster)\n" +
                    "\t// Put this as 0 for accuracy, and something high like 0.98 for speed\n" +
                    "\tif (diffuseIntensity > 0.98)\n" +
                    "\t{\n" +
                    "\t\tfloat fSpec = pow(specularIntensity, 64.0);\n" +
                    "\t\tvFragColour.rgb += vec3(fSpec * specularColour.rgb);\n" +
                    "\t}\n" +
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




    }
}