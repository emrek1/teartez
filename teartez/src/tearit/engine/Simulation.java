package tearit.engine;

import java.awt.Graphics;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;

public abstract class Simulation  {

    public Model model;

    public final static Object lock = new Object();
    
    public ArrayList<Obstacle> balls = new ArrayList<>();
        
    public void setModel(Model model){
        model.owner = this;
        this.model = model;
    }

    
    public void fire(float x, float y, float speed){
        synchronized(lock){
            balls.add(new Obstacle(2, new Vector3(0, 0, -speed), new Vector3(x, y, 3)));        
        }
    }
    
    public abstract void solve();
    public abstract void operate(float f);
    public void render(){}
    public void paint(Graphics g){}

    Particle grabbedParticle;
    
    
    
    
    public void pressed(float x, float y) {
        
    }
    
    public void released(float x, float y) {
    
    }

    public void dragged(float x, float y) {
        
    }
    
    public static FloatBuffer getfb(float[] array){		
        FloatBuffer buf = BufferUtils.createFloatBuffer(4).put(array);
        buf.position(0);
        return buf;
        /*ByteBuffer byteBuf = ByteBuffer.allocateDirect(array.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        FloatBuffer fbuf = byteBuf.asFloatBuffer();
        fbuf.put(array);
        fbuf.position(0);
        return fbuf;*/
    }
    

}
