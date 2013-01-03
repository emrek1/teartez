package tearit.engine;


public class Vector3
{
	public float x;
	public float y;
	public float z;

        Vector3(){}

	public Vector3(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

        public Vector3(Vector3 v){
            x = v.x;
            y = v.y;
            z = v.z;
        }
        
	public Vector3 setVector(Vector3 v){
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public Vector3 add (Vector3 v){
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}

	public Vector3 sub (Vector3 v){
		return new Vector3(x - v.x, y - v.y, z - v.z);
	}

	public Vector3 mul (float value){
		return new Vector3(x * value, y * value, z * value);
	}

	public Vector3 div (float value){
		return new Vector3(x / value, y / value, z / value);
	}

    public Vector3 trim (float maxValue){
        float len = length();
        if(len>maxValue)
            return new Vector3(x * maxValue/len, y * maxValue/len, z * maxValue/len);
        else
        	return new Vector3(x, y, z);
	}

        public void addon (Vector3 v){
		x += v.x;
		y += v.y;
		z += v.z;
	}

	public void subon (Vector3 v){
		x -= v.x;
		y -= v.y;
		z -= v.z;
	}

	public void mulon (float value){
		x *= value;
		y *= value;
		z *= value;
	}
	
	public void divon (float value){
		x /= value;
		y /= value;
		z /= value;
	}

        public void trimon(float maxValue){
            float len = length();
            if(len>maxValue){
                x *= maxValue/len;
                y *= maxValue/len;
                z *= maxValue/len;
            }
        }

	public Vector3 negate (){
		return new Vector3(-x, -y, -z);
	}

	public float length(){
		return (float)Math.sqrt(x*x + y*y + z*z);
	};

	public void normalize(){
		float length = this.length();

		if (length == 0)
			return;

		x /= length;
		y /= length;
		z /= length;
	}

	public Vector3 unit(){
		float length = this.length();

		if (length == 0)
			return new Vector3(0,0,0);

		return new Vector3(x / length, y / length, z / length);
	}

	public String toString(){
		  return "x: "+x+" y: "+y+" z: "+z;
	}

	public float dot(Vector3 u){
	    return  u.x*this.x +
	            u.y*this.y +
	            u.z*this.z;
	}

	
	static float err = 0.2f;
    // is num between n1 and n2
    public static boolean isBtw(float num, float n1, float n2){
    	return (num<=n1+err && num>=n2-err) || (num>=n1-err && num<=n2+err);
    }
        
    @Override
    public boolean equals(Object o2){
    	Vector3 v2 = (Vector3)o2;
    	return x==v2.x && y==v2.y && z==v2.z; 
    	
    }
    

	//C=AXB
	//return C
	public Vector3 cross(Vector3 b){
		return new Vector3(y*b.z - z*b.y, z*b.x - x*b.z, x*b.y - y*b.x);
	}

	public void setSize(float newLen) {
		float length = this.length();

		if (length == 0)
			return;

		x *= newLen/length;
		y *= newLen/length;
		z *= newLen/length;
		
	}
    
    public double angBtw(Vector3 v2){
        return Math.acos((v2.dot(this))/(length()*v2.length()));
    }
    
    //getright while z=1 is up (x-y plane)
    public Vector3 getRight(){
        return this.cross(new Vector3(0.0F, 0.0F, 1.0F)).unit();
    }
    
}
