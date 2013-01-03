package tearit.engine;

import java.util.ArrayList;
import tearit.Tearit;

public class Spring {

    public Model owner;
    public Particle p1, p2;
    public float length;
    public float k;

    public float frictCons = 0.9f;
    public boolean teared;

    public ArrayList<Mesh> nb = new ArrayList<>();

    public Spring(Model model, Particle p1, Particle p2, float length, float k) {
        owner = model;
        this.p1 = p1;
        this.p2 = p2;
        this.length = length;
        this.k = k;
        
        if(!p1.connectedSprings.contains(this))
            p1.connectedSprings.add(this);
        
        if(!p2.connectedSprings.contains(this))
            p2.connectedSprings.add(this);
    }
    
    public Spring(Model model, Particle p1, Particle p2, float k) {
        this(model, p1, p2, p1.pos.sub(p2.pos).length(), k);
    }
    
    float tearCons = 1.7f;
    float tearWeakCons = 1.5f;
    public void solve(){
        Vector3 distance = p1.pos.sub(p2.pos);
        float len = distance.length();
        
        if(len>length){
            if(len>length*tearCons && (!p1.stable && !p2.stable) && length>0.3f){
            //if(len>length*tearCons && length>0.3f){
                System.out.println("len: "+length);
                tear(false);
                return;
            }else if(len>length*tearWeakCons && (!p1.stable && !p2.stable) && length>0.3f && (p1.injured || p2.injured)){
                tear(false);
                return;
            }
        
            distance.normalize();
            distance.mulon((len-length)*k);

            //friction
            distance.addon(p1.vel.sub(p2.vel).mul(frictCons));

            p2.applyForce(distance);
            distance.mulon(-1);
            p1.applyForce(distance);
            if(len>length*10){
            	System.out.println("xxx"+len);
                p1.vel.mulon(0.1f);
                p2.vel.mulon(0.1f);
            }
        }
    }

    public void tear(boolean tearWeak){
        Vector3 distance = p1.pos.sub(p2.pos);
        float len = distance.length();
        distance.normalize();
        float shorten = (len - (0.7f * length * tearCons))/2;

        distance.mulon(shorten);   // 2 olsa tam baslangic boyu
        
        synchronized(Simulation.lock){            
            if(nb.size()==1){
                if(detach(distance))
                    return;
            }
            if(p1.injured || p2.injured){
                if(tearInjured(distance))
                    return;
            }
            if(tearWeak)
                return;
            
            p1.connectedSprings.remove(this);
            p2.connectedSprings.remove(this);
            owner.springs.remove(this);

            //divide the spring in two            
            Vector3 mid1 = p1.pos.add(p2.pos).div(2);
            Vector3 mid2 = new Vector3(mid1);
            
            if(p1.pos.sub(mid1).length()>p1.pos.sub(mid1.add(distance)).length()){            
                mid1.addon(distance);
                mid2.subon(distance);
            }else{
                mid1.subon(distance);
                mid2.addon(distance);
            }
            
            Particle midP1 = new Particle(mid1);
            Particle midP2 = new Particle(mid2);
//            System.out.println("m1: "+mid1);
//            System.out.println("m2: "+mid2);
            Tearit.obj.curx=mid1.x;
            Tearit.obj.cury = mid1.y;
            owner.particles.add(midP1); owner.particles.add(midP2);
            
            Spring s1 = new Spring(owner, p1, midP1, length/2*1.1f, k);
            //System.out.println(p1.pos.sub(mid1).length()+"  < "+(s1.length * tearCons));
            Spring s2 = new Spring(owner, p2, midP2, length/2*1.1f, k);
            //System.out.println(p2.pos.sub(mid2).length()+"  < "+(s2.length * tearCons));
            owner.springs.add(s1);
            owner.springs.add(s2);
            
            ArrayList<Mesh> neighbours = new ArrayList<>(nb);
            for(Mesh m:neighbours){
                m.divide(this, s1, s2);
            }
            
            
            
            Tearit.initData();
        }
    }
    
    boolean detach(Vector3 distance){
        Mesh mesh = nb.get(0);
        if(p1.connectedSprings.size()>2 && mesh.findOtherSpring(p1, this).nb.size()==1){
            Spring s = mesh.findOtherSpring(p1, this);
            Particle p = new Particle(p1.pos.sub(distance));
            owner.particles.add(p);
            p1.connectedSprings.remove(this);
            p1.connectedSprings.remove(s);

            p.connectedSprings.add(this);
            p.connectedSprings.add(s);
            if(s.p1 == p1)
                s.p1=p;
            else
                s.p2=p;                    
            p1=p;  
            mesh.updateParticles();
            return true;
        }else if(p2.connectedSprings.size()>2 && mesh.findOtherSpring(p2, this).nb.size()==1){
            Spring s = mesh.findOtherSpring(p2, this);
            Particle p = new Particle(p2.pos.add(distance));
            owner.particles.add(p);
            p2.connectedSprings.remove(this);
            p2.connectedSprings.remove(s);
            
            p.connectedSprings.add(this);
            p.connectedSprings.add(s);
            if(s.p1 == p2)
                s.p1=p;
            else
                s.p2=p;                    
            p2=p;      
            mesh.updateParticles();   
            return true;
        }
        return false;
    }

    boolean tearInjured(Vector3 distance){
        if(nb.isEmpty()){
            System.out.println("problem: spring left alone nb=0");
            return false;
        }        
        if(p1.injured){
            Particle p = new Particle(p1.pos.sub(distance));            
            injure(p1, p);            
        }else{
            Particle p = new Particle(p2.pos.add(distance));            
            injure(p2, p);            
        }
        return true;
    }
    
    public void injure(Particle pOld, Particle pNew){
        
        if(nb.isEmpty()){
            System.out.println("no neighbour ");
            return;
        }
        
        owner.particles.add(pNew);
        if(nb.size()==1){   //yırtık bir parcadan cekiliyor
            updateParticle(pOld, pNew);
            
            Mesh alone = nb.get(0);

            int step = getCount(alone, pOld);
            step /=3;
            System.out.println("step: "+step);
            
            Spring otherSpring = alone.findOtherSpring(pOld, this);
            Mesh otherMesh = alone; 
            while(true){
                step--;
                if(step>0){
                    //otherSpring = otherMesh.findOtherSpring(pOld, otherSpring);
                    otherSpring.updateParticle(pOld, pNew);  
                    otherMesh.updateParticles();                    
                    otherMesh = otherSpring.findOtherMesh(otherMesh);                    
                    otherSpring = otherMesh.findOtherSpring(pOld, otherSpring);
                }else{
                    if(otherSpring.nb.size()==1){
                        otherSpring.updateParticle(pOld, pNew);                                
                    }else{
                        otherSpring.nb.remove(otherMesh);
                        Mesh otherSideMesh = otherSpring.nb.get(0);
                        otherSideMesh.injured = true;
                        otherSpring.otherPoint(pOld).injured = true;

                        Spring sNew = otherSpring.duplicate(pOld, pNew);
                        owner.springs.add(sNew);
                        sNew.nb.add(otherMesh);
                        otherMesh.springs[otherMesh.getIndex(otherSpring)] = sNew;
                    }
                    otherMesh.updateParticles();
                    break;
                }
            }
            
            /*
            Spring s = alone.findOtherSpring(pOld, this);
            
            if(s.nb.size()==1){
                System.out.println("no other neighbour (alone)");
                s.updateParticle(pOld, pNew);                                
            }else{
                s.nb.remove(alone);
                Mesh otherSideMesh = s.nb.get(0);
                otherSideMesh.injured = true;
                s.otherPoint(pOld).injured = true;

                Spring sNew = s.duplicate(pOld, pNew);
                owner.springs.add(sNew);
                sNew.nb.add(alone);
                alone.springs[alone.getIndex(s)] = sNew;
            }
            alone.updateParticles();*/

        }else{
            updateParticle(pOld, pNew);
            
            Mesh m1 = nb.get(0);
            int c1 = getCount(m1, pOld);
            System.out.println("1. taraf: "+c1);
            Mesh m2 = nb.get(1);
            int c2 = getCount(m2, pOld);
            System.out.println("2. taraf: "+c2);

            Mesh alone = c1<c2?m2:m1;
            
            int step = getCount(alone, pOld);
            step /=3;
            System.out.println("step: "+step);
            
            Spring otherSpring = alone.findOtherSpring(pOld, this);
            Mesh otherMesh = alone; 
            while(true){
                step--;
                if(step>0){
                    //otherSpring = otherMesh.findOtherSpring(pOld, otherSpring);
                    otherSpring.updateParticle(pOld, pNew);  
                    otherMesh.updateParticles();                    
                    otherMesh = otherSpring.findOtherMesh(otherMesh);                    
                    otherSpring = otherMesh.findOtherSpring(pOld, otherSpring);
                }else{
                    if(otherSpring.nb.size()==1){
                        otherSpring.updateParticle(pOld, pNew);                                
                    }else{
                        otherSpring.nb.remove(otherMesh);
                        Mesh otherSideMesh = otherSpring.nb.get(0);
                        otherSideMesh.injured = true;
                        otherSpring.otherPoint(pOld).injured = true;

                        Spring sNew = otherSpring.duplicate(pOld, pNew);
                        owner.springs.add(sNew);
                        sNew.nb.add(otherMesh);
                        otherMesh.springs[otherMesh.getIndex(otherSpring)] = sNew;
                    }
                    otherMesh.updateParticles();
                    break;
                }
            }
            
            
            /*Spring s = alone.findOtherSpring(pOld, this);            
            
            if(s.nb.size()==1){ // directly detach this s string like (this)
                System.out.println("no other neighbour");
                s.updateParticle(pOld, pNew);                                
            }else{
                s.nb.remove(alone);
                Mesh otherSideMesh = s.nb.get(0);
                otherSideMesh.injured = true;
                s.otherPoint(pOld).injured = true;

                Spring sNew = s.duplicate(pOld, pNew);
                owner.springs.add(sNew);
                sNew.nb.add(alone);
                alone.springs[alone.getIndex(s)] = sNew;                
            }
            alone.updateParticles();*/

            otherSpring = this;
            otherMesh = c1<c2?m1:m2;
            while(true){
                if(otherMesh!=null){
                    otherSpring = otherMesh.findOtherSpring(pOld, otherSpring);
                    otherSpring.updateParticle(pOld, pNew);     
                    otherMesh.updateParticles();
                    otherMesh = otherSpring.findOtherMesh(otherMesh);   
                }else{
                    break;
                }
                                 
            }    
        }
    }
    
    
    
    //create a new spring which connects pOldOther - pNew
    Spring duplicate(Particle pOld, Particle pNew){
        pOld.connectedSprings.remove(this);
        if(pOld==p1){            
            return  new Spring(owner, pNew, p2, length, k);
        }else if(pOld==p2){            
            return new Spring(owner, p1, pNew, length, k);
        }else{
            System.out.println("problem in duplicate");
            return null;
        }
    }
    
    
    
    void detach(Vector3 distance, Particle pOld, Particle pNew, ArrayList<Mesh> meshes){
        for(Mesh m:meshes){
            for(Spring s:m.getSpringsConnected(pOld)){
                s.updateParticle(pOld, pNew);
            }
            m.updateParticles();
        }
    }
    
    public String toString(){
        return length+"";
    }

    
    //release pOld, connect that end to pNew
    private void updateParticle(Particle pOld, Particle pNew) {
        if(p1==pOld){            
            p1.connectedSprings.remove(this);
            p1=pNew;
            p1.connectedSprings.add(this);
        }else{
            p2.connectedSprings.remove(this);
            p2=pNew;
            p2.connectedSprings.add(this);
        }    
    }
    
    public int getCount(Mesh m, Particle p){
        int ctr=0;
        Spring otherSpring = this;
        Mesh otherMesh = m;
        while(true){
            ctr++;
            otherSpring = otherMesh.findOtherSpring(p, otherSpring);
            if(otherSpring == null){
                System.out.println("problem getCount");
                return 0;
            }
            otherMesh = otherSpring.findOtherMesh(otherMesh);
            if(otherMesh==null)
                return ctr;
        }
    }
    
    public Mesh findOtherMesh(Mesh m){
        if(nb.size()==1 || nb.isEmpty()){
            return null;
        }
        
        if(m==nb.get(0))
            return nb.get(1);
        else
            return nb.get(0);
    }

    public Particle otherPoint(Particle p) {
        return p==p1?p2:p1;
    }
    
    
}
