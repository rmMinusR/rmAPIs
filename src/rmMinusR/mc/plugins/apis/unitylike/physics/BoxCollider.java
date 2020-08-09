package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.data.Matrix;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class BoxCollider extends CompoundCollider {
	
	public final Vector3 local_offset;
	public final Vector3 size;
	
	public BoxCollider(GameObject gameObject, Vector3 local_offset, Vector3 size) {
		super(gameObject,
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateX(Mathf.PI*0.0f), Matrix.Translate(new Vector3(0, 0, 0.5f)))),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateX(Mathf.PI*0.5f), Matrix.Translate(new Vector3(0, 0, 0.5f)))),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateX(Mathf.PI*1.0f), Matrix.Translate(new Vector3(0, 0, 0.5f)))),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateX(Mathf.PI*1.5f), Matrix.Translate(new Vector3(0, 0, 0.5f)))),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateY(Mathf.PI*0.5f), Matrix.Translate(new Vector3(0, 0, 0.5f)))),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.RotateX(Mathf.PI*1.5f), Matrix.Translate(new Vector3(0, 0, 0.5f))))
			);
		this.local_offset = local_offset;
		this.size = size;
	}
	
}
