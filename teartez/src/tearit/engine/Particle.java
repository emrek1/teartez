package tearit.engine;

import java.util.ArrayList;

public class Particle {

    public float mass = 0.9f;

    public Vector3 pos, vel, force;

    public ArrayList<Spring> connectedSprings = new ArrayList<Spring>();
    public boolean dead;
    public boolean passive;

    public boolean stable;

    public boolean injured;
    
    public Particle(){
        pos = new Vector3();
        vel = new Vector3();
        force = new Vector3();
    }
    
    public Particle(float x, float y, float z){
        pos = new Vector3(x, y, z);
        vel = new Vector3();
        force = new Vector3();
    }
    
    public Particle(Vector3 pos){
        this.pos = pos;
        vel = new Vector3();
        force = new Vector3();
    }

    public Particle(Vector3 pos, Vector3 vel, Vector3 force) {
        this.pos = pos;
        this.vel = vel;
        this.force = force;
    }

    public void operate(float dt){
    	force.mulon(dt/mass);
        vel.addon(force);
        pos.addon(vel.mul(dt));
    }

    public void applyForce(Vector3 appliedForce){
        force.addon(appliedForce);
    }
    
    public String toString(){
        return pos.toString();
    }

}
