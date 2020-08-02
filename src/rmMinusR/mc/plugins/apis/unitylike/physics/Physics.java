package rmMinusR.mc.plugins.apis.unitylike.physics;

import java.util.ArrayList;
import java.util.function.Function;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.unitylike.core.UnitylikeObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class Physics {
	
	private static final Function<Collider, Boolean> selectAny = new Function<Collider, Boolean>() {
		@Override
		public Boolean apply(Collider t) {
			return t != null;
		}
	};
	
	public static boolean Raycast(Vector3 origin, Vector3 direction)										{ return Raycast(origin, direction, Float.MAX_VALUE, selectAny); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, float maxDistance)						{ return Raycast(origin, direction,     maxDistance, selectAny); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, Function<Collider, Boolean> selector)	{ return Raycast(origin, direction, Float.MAX_VALUE, selector ); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, float maxDistance, Function<Collider, Boolean> selector) {
		Ray ray = new Ray(origin, direction);
		
		for(UnitylikeObject o : RmApisPlugin.INSTANCE.unitylikeEnv.FindObjectsOfType(Collider.class)) {
			Collider c = (Collider)o;
			
			if(c.GetCollision(ray) != null) return true;
		}
		
		return false;
	}
	
	public static boolean Linecast(Vector3 start, Vector3 end) { return Linecast(start, end, selectAny); };
	public static boolean Linecast(Vector3 start, Vector3 end, Function<Collider, Boolean> selector) { return Raycast(start, end.Sub(start), end.Sub(start).GetMagnitude(), selector); }
	
	public static RaycastHit[] RaycastAll(Vector3 origin, Vector3 direction)									    { return RaycastAll(origin, direction, Float.MAX_VALUE, selectAny); }
	public static RaycastHit[] RaycastAll(Vector3 origin, Vector3 direction, float maxDistance)						{ return RaycastAll(origin, direction,     maxDistance, selectAny); }
	public static RaycastHit[] RaycastAll(Vector3 origin, Vector3 direction, Function<Collider, Boolean> selector)	{ return RaycastAll(origin, direction, Float.MAX_VALUE, selector ); }
	public static RaycastHit[] RaycastAll(Vector3 origin, Vector3 direction, float maxDistance, Function<Collider, Boolean> selector) {
		ArrayList<RaycastHit> hits = new ArrayList<RaycastHit>();
		Ray ray = new Ray(origin, direction);
		
		for(UnitylikeObject o : RmApisPlugin.INSTANCE.unitylikeEnv.FindObjectsOfType(Collider.class)) {
			Collider c = (Collider)o;
			
			RaycastHit hit = c.GetCollision(ray);
			if(hit != null && Vector3.Distance(origin, hit.point) < maxDistance) hits.add(hit);
		}
		
		RaycastHit[] tmp = null;
		hits.toArray(tmp);
		return tmp;
	}
}
