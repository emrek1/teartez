package tearit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import org.lwjgl.util.glu.Sphere;
import tearit.engine.GravitySimulation;
import tearit.engine.Mesh;
import tearit.engine.Model;
import tearit.engine.Obstacle;
import tearit.engine.Simulation;
import tearit.engine.Spring;
import tearit.engine.Vector3;

public class Tearit extends JFrame implements MouseListener, MouseMotionListener{

    public static final Object lock = new Object();
    public static final int DISPLAY_HEIGHT = 480; //720; //480
    public static final int DISPLAY_WIDTH = 640;  //960;  //640
    int wid=500, heg=500;
    float timeStep = 0.03f;
    
    public static final Logger LOGGER = Logger.getLogger(Tearit.class.getName());
    
    Graphics g;
    Image im;
    
    Sphere ball;
    
    static Simulation sim;
    
    int[] textures = new int[1];
    float[][][] texCoords;
    static float vertices[];
    static float texture[];
    static float normals[];
    
    static FloatBuffer vertexBuffer;
    static FloatBuffer textureBuffer;
    static FloatBuffer normalBuffer;
    static int cpc, till;
    
    static {
        try {
            LOGGER.addHandler(new FileHandler("errors.log", true));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.toString(), ex);
        }
    }

    
    public static Tearit obj;
    public static void main(String[] args) {
        Tearit main = null;
        try {            
            Simulation sim = new GravitySimulation();            
            Model m = new ClothModel3();
            sim.setModel(m);            
            main = new Tearit(sim);     
            obj = main;
            main.create();
            main.run();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        } 
    }
    
    long start;
    public Tearit(Simulation simulation) {
        start = System.currentTimeMillis();
        sim = simulation;
        initData();
        addMouseListener(this);
        addMouseMotionListener(this);
        setSize(wid, heg);
        setVisible(true);
        im = createImage(wid, heg);
        g = im.getGraphics();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
    }

    public void create() throws LWJGLException {
        //Display
        Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
        Display.setFullscreen(false);
        Display.setTitle("Tear it!");
        Display.create();

        //Keyboard
        Keyboard.create();

        //Mouse
        Mouse.setGrabbed(false);
        Mouse.create();

        //OpenGL
        initGL();
        resizeGL();
    }

    
    public static void initData(){
        
        vertices = new float[sim.model.meshes.size() * 3 * 3];  // 3 points for each mesh, 3 data(x,y,z) for each point
        normals = new float[sim.model.meshes.size() * 3 * 3];
        texture = new float[sim.model.meshes.size()*6];
        updateVertices();
        //texCoords = createTexCoords();
        //texture = createTexVertices(texCoords);
        vertexBuffer = giveFloatBuffer(vertices);
        textureBuffer = giveFloatBuffer(texture);
        normalBuffer = giveFloatBuffer(normals);
        cpc = vertexBuffer.capacity();
        till = sim.model.meshes.size() * 3;
        //System.out.println("till:"+till);
    }
    
    public static void updateVertices() {
        
        int ctr=0, nc=0, tc=0;
        for(Mesh m:sim.model.meshes){
            Vector3 normal = m.mesh[1].sub(m.mesh[0]).cross(m.mesh[2].sub(m.mesh[1]));
            normal.normalize();
            
//            if (normal.z < 0) {
//                normal.mulon(-1);
//            }
            
            nc = ctr;
            normals[nc++] = normal.x; normals[nc++] = normal.y; normals[nc++] = normal.z; 
            normals[nc++] = normal.x; normals[nc++] = normal.y; normals[nc++] = normal.z; 
            normals[nc++] = normal.x; normals[nc++] = normal.y; normals[nc++] = normal.z; 
            vertices[ctr++] = m.mesh[0].x; vertices[ctr++] = m.mesh[0].y; vertices[ctr++] = m.mesh[0].z; 
            vertices[ctr++] = m.mesh[1].x; vertices[ctr++] = m.mesh[1].y; vertices[ctr++] = m.mesh[1].z; 
            vertices[ctr++] = m.mesh[2].x; vertices[ctr++] = m.mesh[2].y; vertices[ctr++] = m.mesh[2].z; 
            
            
            texture[tc++] = m.textPos[0][0]; texture[tc++] = m.textPos[0][1];
            texture[tc++] = m.textPos[1][0]; texture[tc++] = m.textPos[1][1];
            texture[tc++] = m.textPos[2][0]; texture[tc++] = m.textPos[2][1];
            
            
        }
        
        

    }

    public static FloatBuffer giveFloatBuffer(float[] array) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(array.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer fbuf = byteBuf.asFloatBuffer();
        fbuf.put(array);
        fbuf.position(0);
        return fbuf;
    }
    
    
    public void initGL() {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //glDisable(GL_DEPTH_TEST);
        //glDisable(GL_LIGHTING);
        
        int textId = loadTexture(); // Load image into Texture
        glEnable(GL_TEXTURE_2D); // Enable texture
        glBindTexture(GL_TEXTURE_2D, textId);
        ball = new Sphere();
    }

    private static final int BYTES_PER_PIXEL = 4;
    public static int loadTexture(){
      BufferedImage image = null;
        try {
            //image = ImageIO.read(Tearit.class.getResourceAsStream("../images/crate.png"));
            image = ImageIO.read(Tearit.class.getResourceAsStream("crate.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
      int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:

        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID
        
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
      
        //Return the texture ID so we can bind it later again
        return textureID;
    }
    
    
    public void processKeyboard() {
        //Square's Size
        /*if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            --squareSize;
        }*/
    }

    boolean pressed;
    
    public void processMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();
        if(Mouse.isButtonDown(0)){
            if(pressed){
                sim.dragged(x, y);
            }else{
                pressed = true;
                sim.pressed(x, y);
            }
        }else if(pressed){
            pressed = false;
            sim.released(x, y);
        }        
    }

    public void render() {
        
        //Clear buffers
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glLoadIdentity();										//reset modelview matrix
	glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        //GLU.gluLookAt(-23, 0, 0, 0, 0, 0, 0, 1, 0);
        GLU.gluLookAt(0, 0, 19, 0, 0, 0, 0, 1, 0);
	

        
        
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        normalBuffer.put(normals);
        normalBuffer.position(0);

        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
        //gl.glBufferData(GL11.GL_ARRAY_BUFFER, cpc, vertexBuffer, GL11.GL_DYNAMIC_DRAW);


        // Enable the vertex, texture and normal state

        glEnableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        // Point to our buffers
        glNormalPointer(0, normalBuffer);
        glNormalPointer(0, normalBuffer);
        glVertexPointer(3, 0, vertexBuffer);
        glTexCoordPointer(2, 0, textureBuffer);


        glDrawArrays(GL_TRIANGLES, 0, till);

        /*
         * int ctr = 0; gl.glNormal3f(0, 0, 1); for(int i = 0; i<till; i++){ gl.glDrawArrays(GL10.GL_TRIANGLES, ctr, 3); ctr += 3;
         }
         */

        
        
        // Disable the client state before leaving
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);

        
        
        ArrayList<Obstacle> balls = new ArrayList<>(sim.balls);
        for(Obstacle o:balls){
            glTranslatef(o.pos.x , o.pos.y, o.pos.z);
            o.draw();
            glTranslatef(-o.pos.x , -o.pos.y, -o.pos.z);
        }
        
        
    }

    int scale = 30;
    public void paint(Graphics g2){
        synchronized(Simulation.lock){
            if(g==null)
                return;
            g.clearRect(0, 0, 1000, 1000);
            ((Graphics2D)g).translate(trans, trans);

            for (Spring sp : sim.model.springs) {
                if (!sp.teared) {
                    g.drawLine((int) (sp.p1.pos.x * scale), -(int) (sp.p1.pos.y * scale), (int) (sp.p2.pos.x * scale), -(int) (sp.p2.pos.y * scale));
                }
                g.setColor(Color.blue);
                g.fillOval((int)(sp.p1.pos.x*scale), (int)(-sp.p1.pos.y*scale), 3, 3);
                //g.setColor(Color.blue);
                g.fillOval((int)(sp.p2.pos.x*scale), (int)(-sp.p2.pos.y*scale), 3, 3);
                g.setColor(Color.black);
            }
            
            //g.fillOval((int)(curx*scale), (int)(-cury*scale), 3, 3);
//            g.drawRect(-2*scale, -2*scale, 4*scale, 4*scale);
//            g.drawRect(-3*scale, -3*scale, 6*scale,6*scale);
//            g.drawRect(-4*scale, -4*scale, 8*scale,8*scale);
//            g.drawRect(-5*scale, -5*scale, 10*scale,10*scale);
            ((Graphics2D)g).translate(-trans, -trans);
            /*for(Mesh m:sim.model.meshes){
                g.drawLine((int)(m.mesh[0].x*scale), -(int)(m.mesh[0].y*scale), (int)(m.mesh[1].x*scale), -(int)(m.mesh[1].y*scale));
                g.drawLine((int)(m.mesh[1].x*scale), -(int)(m.mesh[1].y*scale), (int)(m.mesh[2].x*scale), -(int)(m.mesh[2].y*scale));
                g.drawLine((int)(m.mesh[2].x*scale), -(int)(m.mesh[2].y*scale), (int)(m.mesh[0].x*scale), -(int)(m.mesh[0].y*scale));
            }*/
            g.drawString(""+curx+" "+cury, 30, 670);
        }
        g2.drawImage(im, 0, 0, this);
    }
    
    float[] white = {1.0f, 1.0f, 1.0f, 1.0f};
    float[] black = {0.0f, 0.0f, 0.0f, 0.0f};
    float[] grey = {0.2f, 0.2f, 0.2f, 0.2f};
    float[] red = {1.0f, 0.0f, 0.0f, 1.0f};
    float[] green = {0.0f, 1.0f, 0.0f, 1.0f};
    float[] blue = {0.0f, 0.0f, 1.0f, 1.0f};
    
    
    
    public void resizeGL() {

        
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);

	//Set up projection matrix
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	gluPerspective(45.0f, DISPLAY_WIDTH/DISPLAY_HEIGHT, 1.0f, 100.0f);
        
	//Load identity modelview
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();

	//Shading states
	glShadeModel(GL_SMOOTH);
	glClearColor(0,0,0,0);
	glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

	//Depth states
	glClearDepth(1.0f);
	glDepthFunc(GL_LEQUAL);
	glEnable(GL_DEPTH_TEST);

	//Set up light        
	float[] f = {1.0f, 1.0f, 1.0f};
        glLight(GL_LIGHT1, GL_POSITION, Simulation.getfb(f) );
	glLight(GL_LIGHT1, GL_DIFFUSE, Simulation.getfb(white));
	glLight(GL_LIGHT1, GL_AMBIENT, Simulation.getfb(grey));
	glLight(GL_LIGHT1, GL_SPECULAR, Simulation.getfb(white));
	glEnable(GL_LIGHT1);
        glEnable(GL_LIGHTING);

	//Use 2-sided lighting
	glLightModeli(GL_LIGHT_MODEL_TWO_SIDE, 1);
        System.out.println("light model set");
        /*
        glViewport(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0.0f, DISPLAY_WIDTH, 0.0f, DISPLAY_HEIGHT);
        glPushMatrix();
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();*/
    }

    public void run() {
        last = System.currentTimeMillis();
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            if (Display.isVisible()) {
                update();
                processKeyboard();
                processMouse();
                
                render();
            } else {
                if (Display.isDirty()) {
                    render();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            Display.update();
            //Display.sync(50);
            repaint();
        }
    }

    long last;
    public void update() {
        synchronized(lock){
            long now = System.currentTimeMillis();
        
            long delta = now - last;
            //System.out.println("delta: "+delta);
            last = now;

            delta++;
            delta=(long)(delta*2);

            sim.solve();
            sim.operate(delta/1000f);   
            updateVertices();

            if(System.currentTimeMillis()>5000+start){
                //sim.model.springs.get(5).tear();
                //System.out.println("torn");
                start = System.currentTimeMillis()+10000;
                //initData();
            }
        }
    }

    long lastFrame ;
    public int getDelta() {
        long time = Sys.getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;
        return delta;
    }
    
    public void destroy() {
        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    int trans = 200;
    
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton()==3)
            return;
        float x = e.getX()-trans;
        float y = trans-e.getY();
        x /= scale;
        y /= scale;
        
        sim.pressed(x, y);
        
        //System.out.println("press: "+x+"  "+y);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        float x = e.getX()-trans;
        float y = trans-e.getY();
        x /= scale;
        y /= scale;
        
        sim.released(x, y);
        
        
        if(e.getButton()==3){
            float speed = 0.1f;
            speed = 0.3f;        
            sim.fire(x, y, speed);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        float x = e.getX()-trans;
        float y = trans-e.getY();
        x /= scale;
        y /= scale;
        
        sim.dragged(x, y);
    }

    public float curx, cury;
    @Override
    public void mouseMoved(MouseEvent e) {
        float x = e.getX()-trans;
        float y = trans-e.getY();
        x /= scale;
        y /= scale;
        int fx = (int)(x*100), fy=(int)(y*100);
        //curx = fx/100f;
        //cury = fy/100f;
    }
    
    
}
