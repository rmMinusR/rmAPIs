package rmMinusR.mc.plugins.apis.unitylike.data;

public final class Quaternion implements Cloneable {
	
	public float w, x, y, z;
	
	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Quaternion(double w, double x, double y, double z) {
		this((float) w, (float) x, (float) y, (float) z);
	}
	
	public Vector3 forward() {
		//https://www.gamedev.net/forums/topic/56471-extracting-direction-vectors-from-quaternion/1273785
		return new Vector3(
					2 * (x*z + w*y),
					2 * (y*z - w*x),
					1 - 2 * (x*x + y*y)
				);
	}
	
	public Vector3 up() {
		//https://www.gamedev.net/forums/topic/56471-extracting-direction-vectors-from-quaternion/1273785
		return new Vector3(
					2 * (x*y - w*z),
					1 - 2 * (x*x + z*z),
					2 * (y*z + w*x)
				);
	}
	
	public Vector3 right() { return left().Mul(-1); }
	private Vector3 left() {
		//https://www.gamedev.net/forums/topic/56471-extracting-direction-vectors-from-quaternion/1273785
		return new Vector3(
					1 - 2 * (y*y + z*z),
					2 * (x*y + w*z),
					2 * (x*z - w*y)
				);
	}
	
	@Override
	protected Quaternion clone() {
		return new Quaternion(w, x, y, z);
	}
	
	public Quaternion Inverse() {
		return new Quaternion(w, -x, -y, -z);
	}
	
	public Quaternion Mul(Quaternion other) { return Mul(this, other); }
	public static Quaternion Mul(Quaternion a, Quaternion... etc) {
		if(etc.length == 0) {
			return a;
		} else if(etc.length == 1) {
			Quaternion b = etc[0];
			
			//See https://en.wikipedia.org/wiki/Quaternion#Hamilton_product
			return new Quaternion(
					a.w*b.w - a.x*b.x - a.y*b.y - a.z*b.z,
					a.w*b.x + a.x*b.w + a.y*b.z - a.z*b.y,
					a.w*b.y - a.x*b.z + a.y*b.w + a.z*b.x,
					a.w*b.z + a.x*b.y - a.y*b.x + a.z*b.w
				);
		} else {
			Quaternion[] etc2 = new Quaternion[etc.length-1];
			for(int i = 0; i < etc.length-1; i++) etc2[i] = etc[i+1];
			return Mul(a, Mul(etc[0], etc2));
		}
	}
	
	public static Quaternion FromAxisRotation(Vector3 axis, float ang) {
		//See https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
		return new Quaternion(
					Math.cos(ang/2f),
					Math.sin(ang/2f)*Math.cos(axis.x),
					Math.sin(ang/2f)*Math.cos(axis.y),
					Math.sin(ang/2f)*Math.cos(axis.z)
				);
	}
	
	public static Quaternion FromEulerAngles(Vector3 e) { //Order: Y->X->Z
		//Using https://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/steps/index.htm
		//heading = y
		//attitude = x
		//bank = z
		//c1 = cos y
		//c2 = cos x
		//c3 = cos z
		//s1 = sin y
		//s2 = sin x
		//s3 = sin z
		return new Quaternion(
					Mathf.Cos(e.y/2)*Mathf.Cos(e.x/2)*Mathf.Cos(e.z/2) - Mathf.Sin(e.y/2)*Mathf.Sin(e.x/2)*Mathf.Sin(e.z/2),
					Mathf.Sin(e.y/2)*Mathf.Sin(e.x/2)*Mathf.Cos(e.z/2) + Mathf.Cos(e.y/2)*Mathf.Cos(e.x/2)*Mathf.Sin(e.z/2),
					Mathf.Sin(e.y/2)*Mathf.Cos(e.x/2)*Mathf.Cos(e.z/2) + Mathf.Cos(e.y/2)*Mathf.Sin(e.x/2)*Mathf.Sin(e.z/2),
					Mathf.Cos(e.y/2)*Mathf.Sin(e.x/2)*Mathf.Cos(e.z/2) - Mathf.Sin(e.y/2)*Mathf.Cos(e.x/2)*Mathf.Sin(e.z/2)
				);
	}
	
	public Vector3 ToEulerAngles() { //Order: Y->X->Z
		boolean bankDef = false, headingDef = false, attitudeDef = false; //FIXME Implement nullable. This is such a stupid fix
		float bank = 0, heading = 0, attitude = 0;
		
		//Gimbal lock protection
		//See http://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToEuler/
		if(x*y+z*w == 0.5f) {
			heading = (float) (2*Math.atan2(x, w));
			attitude = 0;
			headingDef = true;
			attitudeDef = true;
		}
		
		if(x*y+z*w == -0.5f) {
			heading = (float) (-2*Math.atan2(x, w));
			attitude = 0;
			headingDef = true;
			attitudeDef = true;
		}
		
		if(!    bankDef)     bank = (float) Math.asin(2*x*y + 2*z*w);
		if(! headingDef)  heading = (float) Math.atan2(2*y*w-2*x*z, 1-2*y*y-2*z*z);
		if(!attitudeDef) attitude = (float) Math.atan2(2*x*w-2*y*z, 1-2*x*x-2*z*z);
		
		return new Vector3(attitude, heading, bank);
	}
	
	public Matrix ToMatrix() {
		//See https://www.euclideanspace.com/maths/geometry/rotations/conversions/quaternionToMatrix/index.htm
		return Matrix.FromArray(
					1-2*y*y-2*z*z,    2*x*y-2*z*w,    2*x*z+2*y*w,
					  2*x*y+2*z*w,  1-2*x*x-2*z*z,    2*y*z+2*x*w,
					  2*x*z-2*y*w,    2*y*z+2*x*w,  1-2*x*x-2*y*y
				).Resize(4);
	}
	
	public static Quaternion Look(Vector3 rel_towards) {
		float yaw = Mathf.Atan2(rel_towards.x, rel_towards.z);
		float pitch = Mathf.Atan2(rel_towards.y, (float)Math.sqrt(rel_towards.x*rel_towards.x+rel_towards.z*rel_towards.z));
		
		Matrix lookYaw   = Matrix.RotateY(yaw  );
		Matrix lookPitch = Matrix.RotateX(pitch);
		
		return new Transform(Matrix.Mul(lookYaw, lookPitch)).GetRotation();
	}
	
}
