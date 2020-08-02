package rmMinusR.mc.plugins.apis.unitylike.data;

import org.bukkit.util.Vector;

public class Vector3 implements Cloneable {
	
	//Fields
	public float x, y, z;

	//Quickref
	public static Vector3    zero() { return new Vector3( 0,  0,  0); }
	public static Vector3     one() { return new Vector3( 1,  1,  1); }
	
	public static Vector3   right() { return new Vector3( 1,  0,  0); }
	public static Vector3      up() { return new Vector3( 0,  1,  0); }
	public static Vector3 forward() { return new Vector3( 0,  0,  1); }
	
	public static Vector3    left() { return new Vector3(-1,  0,  0); }
	public static Vector3    down() { return new Vector3( 0, -1,  0); }
	public static Vector3    back() { return new Vector3( 0,  0, -1); }
	
	//Ctors
	public Vector3(Vector v) { this(v.getX(), v.getY(), v.getZ()); }
	
	public Vector3(double x2, double y2, double z2) { this((float)x2, (float)y2, (float)z2); }
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	//Data IO
	@Override
	public Vector3 clone() {
		return new Vector3(x, y, z);
	}
	
	public Vector toBukkit() {
		return new Vector(x, y, z);
	}
	
	@Override
	public String toString() {
		return String.format("(%f, %f, %f)", x, y, z);
	}
	
	//Basic
	public Vector3 Add(Vector3 other) { return Add(this, other); }
	public static Vector3 Add(Vector3 a, Vector3... etc) {
		if(etc.length == 0) {
			return a.clone();
		} else if(etc.length == 1) {
			return new Vector3(a.x+etc[0].x, a.y+etc[0].y, a.z+etc[0].z);
		} else {
			Vector3[] etc2 = new Vector3[etc.length-1];
			for(int i = 0; i < etc2.length; i++) etc2[i] = etc[i+1];
			return Add(a, Add(etc[0], etc2));
		}
	}
	
	public Vector3 Sub(Vector3 other) { return Sub(this, other); }
	public static Vector3 Sub(Vector3 a, Vector3 b) { return new Vector3(a.x-b.x, a.y-b.y, a.z-b.z); }
	
	public Vector3 Mul(float scalar) { return Mul(this, scalar); }
	public static Vector3 Mul(Vector3 v, float s) { return new Vector3(v.x*s, v.y*s, v.z*s); }
	
	public Vector3 Proj(Vector3 other) { return other.WithMagnitude( this.Dot(other)/this.GetMagnitude()/other.GetMagnitude() ); }
	
	//Pythagorean
	public float GetMagnitude() { return (float) Math.sqrt(x*x + y*y + z*z); }
	public Vector3 WithMagnitude(float m) { return this.Mul(m/GetMagnitude()); }
	public Vector3 Normalize() { return this.WithMagnitude(1); }
	
	public float Distance(Vector3 other) { return Distance(this, other); }
	public static float Distance(Vector3 a, Vector3 b) { return Sub(a, b).GetMagnitude(); }
	
	//Trigonometry
	public float Angle(Vector3 other) { return Angle(this, other); }
	public static float Angle(Vector3 a, Vector3 b) { return (float) Math.acos(a.Dot(b)/a.GetMagnitude()/b.GetMagnitude()); }
	
	//a dot b = |a|*|b| * cos(ang)
	//(a dot b) / (|a|*|b|) = cos(ang)
	//ang = acos( (a dot b) / (|a|*|b|)
	
	//Advanced
	public float Dot(Vector3 other) { return Dot(this, other); }
	public static float Dot(Vector3 a, Vector3 b) { return a.x*b.x + a.y*b.y + a.z*b.z; }
	
	public Vector3 Cross(Vector3 other) { return Cross(this, other); }
	public static Vector3 Cross(Vector3 a, Vector3 b) { return new Vector3(a.y*b.z - a.z*b.y, a.z*b.x - a.y*b.z, a.x*b.y - a.y*b.x); }
	
}
