package rmMinusR.mc.plugins.apis.unitylike.physics;

import java.util.function.Function;

import org.bukkit.Particle;

import rmMinusR.mc.plugins.apis.particle.AdvancedParticleTemplate;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.data.Matrix;
import rmMinusR.mc.plugins.apis.unitylike.data.Plane;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public class QuadCollider extends AbstractCollider {
	
	private Matrix localToWorld;
	private Function<GameObject,Matrix> gameObjectGetLTW;
	public QuadCollider(GameObject gameObject, Matrix localToWorld, Function<GameObject,Matrix> gameObjectGetLTW) {
		super(gameObject);
		this.localToWorld = localToWorld;
		this.gameObjectGetLTW = gameObjectGetLTW;
	}
	
	private Matrix GetLocalToWorld() {
		return Matrix.Mul(gameObjectGetLTW.apply(gameObject), localToWorld);
	}
	
	@Override
	public boolean IsWithin(Vector3 point) {
		Vector3 local_point = GetLocalToWorld().Inverse().TransformPoint(point);
		return Mathf.Approximately(local_point.z, 0) && Mathf.Between(local_point.x, -0.5, 0.5) && Mathf.Between(local_point.y, -0.5, 0.5);
	}

	@Override
	public Vector3 GetClosestPoint(Vector3 global_point) {
		Vector3 local_point = GetLocalToWorld().Inverse().TransformPoint(global_point);
		Vector3 constrained = new Vector3(
					Mathf.Clamp(local_point.x, -0.5f, 0.5f),
					Mathf.Clamp(local_point.y, -0.5f, 0.5f),
					0
				);
		return GetLocalToWorld().TransformPoint(constrained);
	}

	@Override
	public RaycastHit TryRaycast(Line ray, float max_dist) {
		Line local_ray = GetLocalToWorld().Inverse().TransformRay(ray);
		Vector3 hit_loc = new Plane(Vector3.zero(), Vector3.forward()).GetIntercept(local_ray);
		if(Mathf.Between(hit_loc.x, -0.5, 0.5) && Mathf.Between(hit_loc.y, -0.5, 0.5) && local_ray.origin.Distance(hit_loc) < max_dist && local_ray.GetTAt(hit_loc) > 0) {
			RaycastHit out = new RaycastHit();
			out.point = GetLocalToWorld().TransformPoint(hit_loc);
			out.normal = GetLocalToWorld().TransformVector(Vector3.forward());
			out.collider = this;
			return out;
		} else return null;
	}
	
	@Override
	public void DebugRender() {
		ParticleGraphics.wireQuad(gameObject.scene.ref,
					GetLocalToWorld().TransformPoint(new Vector3( 0.5,  0.5, 0)).ToBukkit(),
					GetLocalToWorld().TransformPoint(new Vector3(-0.5,  0.5, 0)).ToBukkit(),
					GetLocalToWorld().TransformPoint(new Vector3(-0.5, -0.5, 0)).ToBukkit(),
					GetLocalToWorld().TransformPoint(new Vector3( 0.5, -0.5, 0)).ToBukkit(),
					new AdvancedParticleTemplate(Particle.FLAME),
					0.1f
			);
	}
	
}
