package tearit.engine;

import java.util.HashMap;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL11.*;


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;


public class GravitySimulation extends Simulation{

    
    public Vector3 gravity = new Vector3(0, -0.9f, 0.0f);
    float airFrictCons = 0.3f;

    float forceLimit = 1000;

    
    @Override
    public void solve() {
        checkCollisions();
        for (Particle p : model.particles) {
            //p.force = new Vector3(0,0,0);   //mevcutu sıfırlamak daha mı iyidir?
            p.force.x = 0;
            p.force.y = 0;
            p.force.z = 0;
            p.applyForce(gravity);
        }
        
        model.solve();
        
        for(Particle p:model.particles){
            p.applyForce(p.vel.mul(-1*airFrictCons));
            if(p.force.length()>forceLimit){
                p.force.trimon(forceLimit);
                System.out.println("kuvvet "+p.force.length());
            }
        }
        
        //ground condition
        /*for (Particle p : model.particles) {
            if (p.vel.y > 0 && p.pos.y > ground) {
                p.vel.y = -p.vel.y / 3;
                p.pos.y = ground;
            }
        }*/
    }
    
    void checkCollisions(){
    	for(Particle p:model.particles){
            for(Obstacle ball:balls){
                Vector3 dist = p.pos.sub(ball.pos);
                    if(dist.length()<ball.radius){
                        p.pos.setVector(ball.pos.add(dist.unit().mul(ball.radius)));
                }
            }
        }
    }

    @Override
    public void operate(float dt) {
        synchronized(lock){
            for (Particle p : model.particles) {
                if (p.passive) {
                    continue;
                }
                p.operate(dt);
                //updateVertices();
            }

            ArrayList<Obstacle> obs = new ArrayList<>(balls);
            for(Obstacle o:obs){
                o.operate(dt);
                if(o.pos.z<-20)
                    balls.remove(o);
            }
            
        }
        
    }
    
    
    
    HashMap<Integer, Particle> grabbedParticles = new HashMap<Integer, Particle>();

    @Override
    public void pressed(float x, float y) {
        for (Particle p : model.particles) {
            if (Math.abs(x - p.pos.x) < 0.3f && Math.abs(y - p.pos.y) < 0.3f) {

                grabbedParticles.put(0, p);
                p.passive = true;
                p.pos.x = x;
                p.pos.y = y;
                break;
            }
        }

        /*
         * int action = e.getAction(); int pid = action >> MotionEvent.ACTION_POINTER_ID_SHIFT; int id = e.getPointerId(pid); int pointerCount = e.getPointerCount(); for(int i=0;i<pointerCount; i++){
         * int k = e.getPointerId(i); }
         *
         * float x = e.getX(pid); float y = e.getY(pid); * if(Vector3.isBtw(x, 0, restartWidth) && Vector3.isBtw(y, restartY-10, restartY+40)){ models.clear(); new SimpleCloth(this); return; }
         *
         * for(Particle p:models.get(0).particles){ if(Math.abs(x-p.pos.x)<15 && Math.abs(y-p.pos.y)<15 ){
         *
         * grabbedParticles.put(id, p); //System.out.println(p.pos+" added to id "+id); p.passive = true; p.pos.x = e.getX(pid); p.pos.y = e.getY(pid); break; } }
         */
    }

    @Override
    public void released(float x, float y) {
        /*
         * int action = e.getAction(); int pid = action >> MotionEvent.ACTION_POINTER_ID_SHIFT; int id = e.getPointerId(pid); releaseParticle(id);
         */
        releaseParticle(0);
    }

    void releaseParticle(int index) {
        Particle p = grabbedParticles.remove(index);
        if (p != null) {
            p.passive = false;
            //System.out.println(p.pos+" released");
        } else {
            //System.out.println("no grabbed particle at index " + index + " to release");
        }

    }

    public void dragged(float x, float y) {

        Particle p = grabbedParticles.get(0);
        if (p != null) {
            dragParticle(p, new Vector3(x, y, 0));
        }

        /*
         * int pointerCount = e.getPointerCount(); for(int i=0;i<pointerCount; i++){ int k = e.getPointerId(i); Particle p = grabbedParticles.get(k); if(p==null) continue; dragParticle(p, new
         * Vector3(e.getX(i), e.getY(i), 0)); }
         */
    }

    void dragParticle(Particle draggedParticle, Vector3 pos) {
        draggedParticle.pos.x = pos.x;
        draggedParticle.pos.y = pos.y;
    }

    
    
}
