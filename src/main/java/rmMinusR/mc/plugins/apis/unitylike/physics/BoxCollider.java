package rmMinusR.mc.plugins.apis.unitylike.physics;

import java.util.function.Function;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.data.Matrix;
import rmMinusR.mc.plugins.apis.unitylike.data.Quaternion;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class BoxCollider extends CompoundCollider {
	
	public final Vector3 local_offset;
	public final Vector3 size;
	
	public static final Function<GameObject,Matrix> getLTW_Default = new Function<GameObject, Matrix>() {
		@Override
		public Matrix apply(GameObject go) {
			return go.GetTransform().GetLocalToWorldMatrix();
		}
	};
	
	@Deprecated //Doesn't work until Transform.ResetRotation() is patched
	public static final Function<GameObject,Matrix> getLTW_NoRotation = new Function<GameObject, Matrix>() {
		@Override
		public Matrix apply(GameObject go) {
			Transform t = go.GetTransform().clone();
			t.ResetRotation();
			return t.GetLocalToWorldMatrix();
		}
	};
	
	public static final Function<GameObject,Matrix> getLTW_YawOnly = new Function<GameObject, Matrix>() {
		@Override
		public Matrix apply(GameObject go) {
			Vector3 goEulerAngles = go.GetTransform().GetRotation().ToEulerAngles();
			Vector3 yawOnlyAngles = new Vector3(0, goEulerAngles.y, 0);
			Transform t = new Transform(go.GetTransform().GetPosition(), Quaternion.FromEulerAngles(yawOnlyAngles));
			return t.GetLocalToWorldMatrix();
		}
	};
	
	public BoxCollider(GameObject gameObject, Vector3 local_offset, Vector3 size, Function<GameObject,Matrix> modeLTW) {
		super(gameObject,
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateX(Mathf.PI*0.0f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateX(Mathf.PI*0.5f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateX(Mathf.PI*1.0f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateX(Mathf.PI*1.5f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateY(Mathf.PI*0.5f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW),
				new QuadCollider(gameObject, Matrix.Mul(Matrix.Translate(local_offset), Matrix.Scale(size), Matrix.RotateY(Mathf.PI*1.5f), Matrix.Translate(new Vector3(0, 0, 0.5f))), modeLTW)
			);
		this.local_offset = local_offset;
		this.size = size;
	}
	
}
