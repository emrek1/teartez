package tearit.engine;

import org.lwjgl.util.glu.Sphere;

public class Obstacle {

    Sphere sphere;
    float radius;
    public Vector3 vel, pos;

    public Obstacle(float radius, Vector3 vel, Vector3 pos) {
        this.radius = radius;
        sphere = new Sphere();
        this.vel = vel;
        this.pos = pos;
    }
    
    public void operate(float dt){
        pos.addon(vel.mul(dt));
    }

    public void draw() {
        sphere.draw(radius*0.9f, 15, 15);
    }
    
    
    
}
