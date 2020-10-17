package rmMinusR.mc.plugins.apis.unitylike.data;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;

public final class MatrixTransform extends Component implements Cloneable, Transform {
	
	public Matrix matrix;
	
	public MatrixTransform() { this((GameObject)null); }
	public MatrixTransform(GameObject gameObject) {
		super(gameObject);
		matrix = Matrix.Identity(4);
	}
	
	protected MatrixTransform(Matrix mat) {
		super(null);
		this.matrix = mat;
	}
	
	public MatrixTransform(Vector pos) { this((GameObject)null, pos); }
	public MatrixTransform(GameObject gameObject, Vector pos) { this(new Vector3(pos)); }
	
	public MatrixTransform(Vector3 pos) { this((GameObject)null, pos); }
	public MatrixTransform(GameObject gameObject, Vector3 pos) {
		super(gameObject);
		matrix = Matrix.Identity(4);
		matrix.m[3][0] = pos.x;
		matrix.m[3][1] = pos.y;
		matrix.m[3][2] = pos.z;
	}
	
	public MatrixTransform(Location loc) { this((GameObject)null, loc); }
	public MatrixTransform(GameObject gameObject, Location loc) {
		super(gameObject);
		
		Matrix rt_pos = Matrix.Translate(new Vector3(loc.toVector()));
		
		Matrix lookYaw   = Matrix.RotateY((float)Math.toRadians( loc.getYaw  () ));
		Matrix lookPitch = Matrix.RotateX((float)Math.toRadians(-loc.getPitch() ));
		
		matrix = Matrix.Mul(rt_pos, lookYaw, lookPitch);
	}
	
	public MatrixTransform(Vector3 pos, Quaternion look) { this((GameObject)null, pos, look); }
	public MatrixTransform(GameObject gameObject, Vector3 pos, Quaternion look) {
		super(gameObject);
		
		Matrix m_pos = Matrix.Translate(pos);
		Matrix m_look = look.ToMatrix();
		
		matrix = Matrix.Mul(m_pos, m_look);
	}
	
	public Matrix WorldToLocal() { return matrix.Inverse(); }
	public Matrix LocalToWorld() { return matrix.clone();   }
	
	public void WriteTo(Location loc) {
		Vector3 pos = GetPosition();
		loc.setX(pos.x); loc.setY(pos.y); loc.setZ(pos.z);
		
		//Quaternion rot = GetRotation();
		loc.setDirection(Forward().ToBukkit());
	}
	
	@Override
	public MatrixTransform clone() {
		return new MatrixTransform(matrix.clone());
	}

	public Vector3 Right() {
		return new Vector3(
				matrix.m[0][0],
				matrix.m[0][1],
				matrix.m[0][2]
		);
	}

	public Vector3 Up() {
		return new Vector3(
				matrix.m[1][0],
				matrix.m[1][1],
				matrix.m[1][2]
		);
	}

	public Vector3 Forward() {
		return new Vector3(
				matrix.m[2][0],
				matrix.m[2][1],
				matrix.m[2][2]
		);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void CopyDataFrom(Transform t) {
		matrix.CopyDataFrom(t.LocalToWorld());
	}

	public Vector3 GetPosition() { return new Vector3(matrix.m[3][0], matrix.m[3][1], matrix.m[3][2]); }
	public void SetPosition(Vector3 pos) { matrix.m[3][0] = pos.x; matrix.m[3][1] = pos.y; matrix.m[3][2] = pos.z; }
	
	public Quaternion GetRotation() {
		float w = (float)Math.sqrt(1+matrix.m[0][0] + matrix.m[1][1] + matrix.m[2][2]) / 2f;
		
		//See https://math.stackexchange.com/questions/237369/given-this-transformation-matrix-how-do-i-decompose-it-into-translation-rotati/417813
		//for why this is necessary
		Vector3 scl_part = GetScale();
		
		Matrix rot_part = matrix.Resize(3);
		for(int i = 0; i < rot_part.size; i++) {
			rot_part.m[0][i] /= scl_part.x;
			rot_part.m[1][i] /= scl_part.y;
			rot_part.m[2][i] /= scl_part.z;
		}
		
		Matrix o = rot_part.Resize(4).Reortho();
		
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
	
	@Deprecated //FIXME it no work
	public void ResetRotation() {
		matrix.CopyDataFrom(
					Matrix.Mul( GetRotation().ToMatrix().Inverse(), matrix )
				);
	}
	
	@Deprecated //FIXME doesn't work until ResetRotation() is patched
	public void SetRotation(Quaternion q) {
		matrix.CopyDataFrom(
					Matrix.Mul( q.ToMatrix(), GetRotation().Inverse().ToMatrix(), matrix )
				);
	}
	
	public Vector3 GetScale() {
		//See https://math.stackexchange.com/questions/237369/given-this-transformation-matrix-how-do-i-decompose-it-into-translation-rotati/417813
		return new Vector3(
					Right().GetMagnitude(),
					Up().GetMagnitude(),
					Forward().GetMagnitude()
				);
	}

	public void SetScale(Vector3 scl) {
		Vector3 r = Right  ().WithMagnitude(scl.x);
		Vector3 u = Up     ().WithMagnitude(scl.y);
		Vector3 f = Forward().WithMagnitude(scl.z);

		matrix.m[0][0] = r.x; matrix.m[0][1] = r.y; matrix.m[0][2] = r.z;
		matrix.m[1][0] = u.x; matrix.m[1][1] = u.y; matrix.m[1][2] = u.z;
		matrix.m[2][0] = f.x; matrix.m[2][1] = f.y; matrix.m[2][2] = f.z;
	}
	
}
