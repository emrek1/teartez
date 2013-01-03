package tearit;

import tearit.engine.Mesh;
import tearit.engine.Model;
import tearit.engine.Particle;
import tearit.engine.Spring;

public class ClothModel1 extends Model{

    public ClothModel1() {
        left = -2;
        top = 2;
        width = 4;
        height = 4;
        
        Particle p1 = new Particle(2, 2, 0);
        Particle p2 = new Particle(2, -2, 0);
        Particle p3 = new Particle(-2, -2, 0);
        Particle p4 = new Particle(-2, 2, 0);
        
        Spring s1 = new Spring(this, p1, p2, 4, 30);
        Spring s2 = new Spring(this, p2, p3, 4, 30);
        Spring s3 = new Spring(this, p3, p4, 4, 30);
        Spring s4 = new Spring(this, p4, p1, 4, 30);
        Spring s5 = new Spring(this, p1, p3, 5.64f, 30);
        
        p1.passive = true;
        p1.stable = true;
        p4.passive = true;
        p4.stable = true;
        
        Mesh m1 = new Mesh(s1, s2, s5, this);
        Mesh m2 = new Mesh(s3, s4, s5, this);
        
        particles.add(p1); particles.add(p2); particles.add(p3); particles.add(p4); 
        springs.add(s1); springs.add(s2); springs.add(s3); springs.add(s4); springs.add(s5); 
        meshes.add(m1); meshes.add(m2); 
        
        
    }
    
}
