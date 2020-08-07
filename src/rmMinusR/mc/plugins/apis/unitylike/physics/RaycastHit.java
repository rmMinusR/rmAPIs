package rmMinusR.mc.plugins.apis.unitylike.physics;

import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class RaycastHit {
	
	public Vector3 point;
	public Vector3 normal;
	public AbstractCollider collider;
	
	public RaycastHit() {}
	
	@Override
	public String toString() {
		return "RaycastHit against "+collider+" at pos="+point+", normal="+normal;
	}
	
}
