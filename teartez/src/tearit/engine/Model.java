package tearit.engine;

import java.util.ArrayList;

public abstract class Model {

    public Simulation owner;
    public ArrayList<Spring> springs = new ArrayList<>();
    public ArrayList<Particle> particles = new ArrayList<>();
    public ArrayList<Mesh> meshes = new ArrayList<>();

    
    // temporary restriction
    // model is assumed a rectangular shape placed on x-y axis
    public float left, top, width, height;     // used for texture positioning

    public void solve(){
        synchronized(owner.lock){
            ArrayList<Spring> spr = new ArrayList<>(springs);
            for(Spring s:spr){
                s.solve();
            }
        }
    }
}
