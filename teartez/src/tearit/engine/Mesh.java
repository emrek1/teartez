package tearit.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Mesh {
    
    public Particle[] particles = new Particle[3];
    public Vector3[] mesh = new Vector3[3];    
    public float[][] textPos = new float[3][2];
    public Spring[] springs = new Spring[3];
    
    boolean injured;
    
    public Mesh(Spring s1, Spring s2, Spring s3, Model model) {
        
        s1.nb.add(this);
        s2.nb.add(this);
        s3.nb.add(this);
        
        springs[0] = s1;
        springs[1] = s2;
        springs[2] = s3;
        
        particles[0] = s1.p1;
        particles[1] = s1.p2;
        particles[2] = (s2.p1.equals(s1.p1) || s2.p1.equals(s1.p2))?s2.p2:s2.p1;
        
        mesh[0] = particles[0].pos;
        mesh[1] = particles[1].pos;
        mesh[2] = particles[2].pos;
        
        textPos[0][0] = (particles[0].pos.x-model.left)/model.width;
        textPos[0][1] = -(particles[0].pos.y-model.top)/model.height;
        
        textPos[1][0] = (particles[1].pos.x-model.left)/model.width;
        textPos[1][1] = -(particles[1].pos.y-model.top)/model.height;
        
        textPos[2][0] = (particles[2].pos.x-model.left)/model.width;
        textPos[2][1] = -(particles[2].pos.y-model.top)/model.height;        
        
    }
    
    public Mesh(Spring s1, Spring s2, Spring s3) {
        
        s1.nb.add(this);
        s2.nb.add(this);
        s3.nb.add(this);
        
        springs[0] = s1;
        springs[1] = s2;
        springs[2] = s3;
        
        particles[0] = s1.p1;
        particles[1] = s1.p2;
        particles[2] = (s2.p1.equals(s1.p1) || s2.p1.equals(s1.p2))?s2.p2:s2.p1;
        
        mesh[0] = particles[0].pos;
        mesh[1] = particles[1].pos;
        mesh[2] = particles[2].pos;
    }

    public void updateParticles(){
        
        
        
        Spring s1 = springs[0];
        Spring s2 = springs[1];
        Spring s3 = springs[2];
        
        Set set = new HashSet();
        set.add(s1.p1);
        set.add(s1.p2);
        set.add(s2.p1);
        set.add(s2.p2);
        set.add(s3.p1);
        set.add(s3.p2);
        
        if(set.size()!=3){
            System.out.println("mesh structure corrupted: particle count: "+set.size());
        }
        
        
        particles[0] = s1.p1;
        particles[1] = s1.p2;
        particles[2] = (s2.p1.equals(s1.p1) || s2.p1.equals(s1.p2))?s2.p2:s2.p1;
        mesh[0] = particles[0].pos;
        mesh[1] = particles[1].pos;
        mesh[2] = particles[2].pos;
    }
    
    //Tear torn to s1 and s2
    void divide(Spring torn, Spring s1, Spring s2) {
        
        Spring edge1 = null, edge2 = null;
        for(Spring s:springs){
            s.nb.remove(this);
            if(!s.equals(torn) && (s.p1==s1.p1 || s.p2==s1.p1)){
                edge1 = s;
            }
            if(!s.equals(torn) && (s.p1==s2.p1 || s.p2==s2.p1)){
                edge2 = s;
            }
        }

        Model model = torn.owner;

        Particle tornEnd1 = torn.p1; //s1.p1.pos;
        Particle tornEnd2 = torn.p2; //s2.p1.pos;

        Particle mid1 = s1.p2;
        Particle mid2 = s2.p2;

        Particle corner = null;
        if(particles[0].equals(tornEnd1) || particles[0].equals(tornEnd2)){
            if(particles[1].equals(tornEnd1) || particles[1].equals(tornEnd2)){
                corner = particles[2];
            }else corner = particles[1];
        }else
            corner = particles[0];
        
        corner.injured = true;

        // this mesh will be removed then 4 springs, 2 meshes and 2 particles will be added
        model.meshes.remove(this);

        
        
        float len = (float)( Math.sqrt( (edge1.length*edge1.length + edge2.length*edge2.length - (torn.length*torn.length/2))/2 ) );
        //Spring sCorner1 = new Spring(model, mid1, corner, corner.pos.sub(mid1.pos).length(), torn.k);
        //Spring sCorner2 = new Spring(model, mid2, corner, corner.pos.sub(mid2.pos).length(), torn.k);
        Spring sCorner1 = new Spring(model, mid1, corner, len*1.0f, torn.k*2);
        Spring sCorner2 = new Spring(model, mid2, corner, len*1.0f, torn.k*2);

        model.springs.add(sCorner1);
        model.springs.add(sCorner2);

        Mesh m1 = new Mesh(s1, sCorner1, edge1); m1.injured=true;
        Mesh m2 = new Mesh(s2, sCorner2, edge2); m2.injured=true;

        m1.textPos[1][0] = (textPos[getIndex(tornEnd1)][0] + textPos[getIndex(tornEnd2)][0])/2;
        m1.textPos[1][1] = (textPos[getIndex(tornEnd1)][1] + textPos[getIndex(tornEnd2)][1])/2;
        m2.textPos[1][0] = m1.textPos[1][0];
        m2.textPos[1][1] = m1.textPos[1][1];

        m1.textPos[2] = (textPos[getIndex(corner)]);
        m1.textPos[0] = (textPos[getIndex(tornEnd1)]);

        m2.textPos[2] = (textPos[getIndex(corner)]);
        m2.textPos[0] = (textPos[getIndex(tornEnd2)]);

        model.meshes.add(m1);
        model.meshes.add(m2);

        
    }
    
    int getIndex(Particle p){
        if(p == particles[0]) return 0;
        if(p == particles[1]) return 1;
        else return 2;
    }
    
    int getIndex(Spring s){
        if(s == springs[0]) return 0;
        if(s == springs[1]) return 1;
        else return 2;
    }

    //find other spring connected to p, other than edge
    Spring findOtherSpring(Particle p, Spring edge) {
        for(Spring s:springs){
            if(edge != s && (s.p1==p || s.p2==p)){
                return s;
            }
        }
        System.out.println("problem: findotherspring in mesh");
        return null;
    }

    
    
    ArrayList<Spring> getSpringsConnected(Particle p) {
        ArrayList<Spring> list = new ArrayList<>();
        for(Spring s:springs){
            if(s.p1==p || s.p2==p)
                list.add(s);
        }
        return list;
    }
    
}
