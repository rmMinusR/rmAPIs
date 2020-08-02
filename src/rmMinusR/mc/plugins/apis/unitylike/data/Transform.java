package rmMinusR.mc.plugins.apis.unitylike.data;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class Transform {
	
	public Matrix matrix;
	
	public Transform() {
		matrix = Matrix.Identity(4);
	}
	
	public Transform(Vector pos) { this(new Vector3(pos)); }
	
	public Transform(Vector3 pos) {
		matrix = Matrix.Identity(4);
		matrix.m[3][0] = pos.x;
		matrix.m[3][1] = pos.y;
		matrix.m[3][2] = pos.z;
	}
	
	public Transform(Location loc) {
		Matrix rt_pos = Matrix.Translate(new Vector3(loc.toVector()));
		
		Matrix lookYaw   = Matrix.RotateY((float)Math.toRadians( loc.getYaw  () ));
		Matrix lookPitch = Matrix.RotateX((float)Math.toRadians(-loc.getPitch() ));
		
		matrix = Matrix.Mul(rt_pos, lookYaw, lookPitch);
	}
	
	public Transform(Vector3 pos, Quaternion look) {
		Matrix m_pos = Matrix.Translate(pos);
		Matrix m_look = look.ToMatrix();
		
		matrix = Matrix.Mul(m_pos, m_look);
	}
	
	public Transform(Vector3 pos, Quaternion look, Vector3 localScale) {
		Matrix m_pos = Matrix.Translate(pos);
		Matrix m_look = look.ToMatrix();
		Matrix m_scl = Matrix.Scale(localScale);
		
		matrix = Matrix.Mul(m_pos, m_look, m_scl);
	}
	
	public Matrix GetWorldToLocalMatrix() { return matrix.clone();   }
	public Matrix GetLocalToWorldMatrix() { return matrix.Inverse(); }
	
	public Vector3   right() { return matrix.TransformVector(Vector3.  right()); }
	public Vector3      up() { return matrix.TransformVector(Vector3.     up()); }
	public Vector3 forward() { return matrix.TransformVector(Vector3.forward()); }
	
	public Vector3 GetPosition() { return new Vector3(matrix.m[3][0], matrix.m[3][1], matrix.m[3][2]); }
	public void SetPosition(Vector3 pos) { matrix.m[3][0] = pos.x; matrix.m[3][1] = pos.y; matrix.m[3][2] = pos.z; }
	
	public Quaternion GetRotation() {
		float w = (float)Math.sqrt(1+matrix.m[0][0] + matrix.m[1][1] + matrix.m[2][2]) / 2f;
		
		Matrix o = matrix.Reortho();
		
		/*
		 * See http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/
		 * NOTE: Only works with an orthogonal matrix, so we have to reorthogonalize first
		 * 
		 * Indexing style: math (YX)
		 * 
		 * qw= sqrt(1 + m00 + m11 + m22) /2
		 * qx = (m21 - m12)/( 4 *qw)
		 * qy = (m02 - m20)/( 4 *qw)
		 * qz = (m10 - m01)/( 4 *qw)
		 * 
		 * Also maths indexing is stupid. Swap XY
		 */
		return new Quaternion(
					w,
					(o.m[1][2]-o.m[2][1])/w/4f,
					(o.m[2][0]-o.m[0][2])/w/4f,
					(o.m[0][1]-o.m[1][0])/w/4f
				);
	}
	
	public void ResetRotation() {
		matrix.CopyFrom(
					Matrix.Mul( GetRotation().ToMatrix().Inverse(), matrix )
				);
	}
	
	public void SetRotation(Quaternion q) {
		matrix.CopyFrom(
					Matrix.Mul( q.ToMatrix(), GetRotation().Inverse().ToMatrix(), matrix )
				);
	}
	
	public Vector3 GetPseudoscale() { return new Vector3(right().GetMagnitude(), up().GetMagnitude(), forward().GetMagnitude()); }
	
}
