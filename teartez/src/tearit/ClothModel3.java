package tearit;

import tearit.engine.Mesh;
import tearit.engine.Model;
import tearit.engine.Particle;
import tearit.engine.Spring;

public class ClothModel3 extends Model{

    float startX=-3, startY=3, edge = 0.3f;
    int pWid=30, pHeg=25; 
    float k = 330;
    
    public ClothModel3(){
        float shortEdge = edge*((float)Math.sqrt(3))/2;
        left = startX;
        top = startY;
        width = edge*(pWid-1)-edge/2;
        height = shortEdge*(pHeg-1);
        
        
        
        Particle[][] particleArray = new Particle[pHeg][pWid];
        
        float posX=startX, posY=startY;
        for (int i = 0; i < pHeg; i++) {            
            for (int j = 0; j < pWid; j++) {                
                particleArray[i][j] = new Particle(posX, posY, 0);                
                particles.add(particleArray[i][j]);
                if(i!=0 && j!=0){
                    /*if(i<2)
                        k=130;
                    else
                        k=50;*/
                    if(i%2==1){
                        Spring s1 = getSpring(particleArray[i][j], particleArray[i-1][j-1]);
                        Spring s2 = getSpring(particleArray[i-1][j-1], particleArray[i][j-1]);
                        Spring s3 = getSpring(particleArray[i][j-1], particleArray[i][j]);
                        Mesh m1 = new Mesh(s1, s2, s3, this);

                        Spring s4 = getSpring(particleArray[i][j], particleArray[i-1][j]);
                        Spring s5 = getSpring(particleArray[i-1][j], particleArray[i-1][j-1]);
                        Mesh m2 = new Mesh(s1, s4, s5, this);
                        
                        meshes.add(m1); meshes.add(m2);
                    }else{
                        Spring s1 = getSpring(particleArray[i][j-1], particleArray[i-1][j]);
                        Spring s2 = getSpring(particleArray[i-1][j], particleArray[i-1][j-1]);
                        Spring s3 = getSpring(particleArray[i-1][j-1], particleArray[i][j-1]);
                        Mesh m1 = new Mesh(s1, s2, s3, this);

                        Spring s4 = getSpring(particleArray[i][j-1], particleArray[i][j]);
                        Spring s5 = getSpring(particleArray[i][j], particleArray[i-1][j]);
                        Mesh m2 = new Mesh(s1, s4, s5, this);
                         
                        meshes.add(m1); meshes.add(m2);
                    }
                }                
                posX += edge;
                if((i%2==0 && j==(pWid-2)) || (i%2==1 && j==0)) posX -= edge/2;
            }            
            posY -= shortEdge;
            posX = startX;
        }
        
        
        for(int idx=0; idx<pWid; idx++){        
            particleArray[0][idx].passive = true;
            particleArray[0][idx].stable = true;            
        }
        
    }
    
    
    public Spring getSpring(Particle p1, Particle p2){
        for(Spring s:springs){
            if((s.p1==p1 && s.p2==p2) || (s.p1==p2 && s.p2==p1)){
                return s;
            }
        }
        Spring s = new Spring(this, p1, p2, k);
        springs.add(s);
        return s;
    }
    
    
    
}
