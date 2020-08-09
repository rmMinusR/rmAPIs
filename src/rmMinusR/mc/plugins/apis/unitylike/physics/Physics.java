package rmMinusR.mc.plugins.apis.unitylike.physics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.function.Function;

import org.bukkit.World;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.unitylike.core.UnitylikeObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class Physics {
	
	public static final Function<AbstractCollider, Boolean> selectAny = new Function<AbstractCollider, Boolean>() {
		@Override
		public Boolean apply(AbstractCollider t) {
			return t != null;
		}
	};
	
	public static final float defaultDistance = 32f;
	
	public static boolean Raycast(Vector3 origin, Vector3 direction)										{ return Raycast(origin, direction, defaultDistance, selectAny); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, float maxDistance)						{ return Raycast(origin, direction,     maxDistance, selectAny); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, Function<AbstractCollider, Boolean> selector)	{ return Raycast(origin, direction, defaultDistance, selector ); }
	public static boolean Raycast(Vector3 origin, Vector3 direction, float maxDistance, Function<AbstractCollider, Boolean> selector) {
		HashSet<AbstractCollider> colliders = new HashSet<AbstractCollider>();
		for(UnitylikeObject o : RmApisPlugin.INSTANCE.unitylikeEnv.FindObjectsOfType(AbstractCollider.class)) if(selector.apply((AbstractCollider) o)) colliders.add((AbstractCollider) o);
		return Raycast(origin, direction, maxDistance, colliders);
	}
	public static boolean Raycast(Vector3 origin, Vector3 direction, float maxDistance, Collection<AbstractCollider> colliders) {
		Line ray = new Line(origin, direction);
		
		for(AbstractCollider c : colliders) {
			if(c.TryRaycast(ray) != null) return true;
		}
		
		return false;
	}
	
	public static boolean Linecast(Vector3 start, Vector3 end) { return Linecast(start, end, selectAny); };
	public static boolean Linecast(Vector3 start, Vector3 end, Function<AbstractCollider, Boolean> selector) { return Raycast(start, end.Sub(start), end.Sub(start).GetMagnitude(), selector); }
	
	public static RaycastHit[] RaycastAll(World world, Vector3 origin, Vector3 direction)									            { return RaycastAll(world, origin, direction, defaultDistance, selectAny); }
	public static RaycastHit[] RaycastAll(World world, Vector3 origin, Vector3 direction, float maxDistance)						    { return RaycastAll(world, origin, direction,     maxDistance, selectAny); }
	public static RaycastHit[] RaycastAll(World world, Vector3 origin, Vector3 direction, Function<AbstractCollider, Boolean> selector)	{ return RaycastAll(world, origin, direction, defaultDistance, selector ); }
	public static RaycastHit[] RaycastAll(World world, Vector3 origin, Vector3 direction, float maxDistance, Function<AbstractCollider, Boolean> selector) {
		HashSet<AbstractCollider> colliders = new HashSet<AbstractCollider>();
		for(UnitylikeObject o : RmApisPlugin.INSTANCE.unitylikeEnv.FindObjectsOfType(AbstractCollider.class)) if(((AbstractCollider)o).gameObject.world == world && selector.apply((AbstractCollider) o)) colliders.add((AbstractCollider) o);
		return RaycastAll(origin, direction, maxDistance, colliders);
	}
	public static RaycastHit[] RaycastAll(final Vector3 origin, final Vector3 direction, float maxDistance, Collection<? extends AbstractCollider> colliders) {
		ArrayList<RaycastHit> hits = new ArrayList<RaycastHit>();
		Line ray = new Line(origin, direction);
		
		System.out.println();
		for(AbstractCollider c : colliders) {
			System.out.println("Attempting raycast on "+c);
			RaycastHit hit = c.TryRaycast(ray);
			if(hit != null && Vector3.Distance(origin, hit.point) < maxDistance && ray.GetTAt(hit.point) > 0) hits.add(hit);
		}
		
		hits.sort(new Comparator<RaycastHit>() {
			@Override
			public int compare(RaycastHit a, RaycastHit b) {
				float da = Vector3.Distance(origin, a.point);
				float db = Vector3.Distance(origin, b.point);
				if(da > db) return 1;
				if(da < db) return -1;
				return 0;
			}
		});
		
		RaycastHit[] tmp = new RaycastHit[hits.size()];
		hits.toArray(tmp);
		return tmp;
	}
}
