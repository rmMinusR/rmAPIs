package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.core.UnitylikeEnvironment;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;

public class WrappedEntity extends GameObject {
	
	public Entity entity;
	
	public WrappedEntity(Entity entity) {
		super(entity.getWorld());
		this.entity = entity;
		
		AddComponent(new EntityCollider(this));
	}
	
	@Override
	public Transform GetTransform() { return new Transform(entity.getLocation()); } //TODO replace with EntityTransform
	@Override
	public void SetTransform(Transform t) { t.WriteTo(entity.getLocation()); }
	
	@Override
	public <T extends Component> T AddComponent(T c) {
		//Only allow non-Transform components
		if(Transform.class.isAssignableFrom(c.getClass())) throw new IllegalArgumentException("Cannot add a Transform to WrappedEntity");
		return super.AddComponent(c);
	}
	
	@Override
	public String toString() {
		return "Wrapped entity: "+entity.toString();
	}
	
	public static WrappedEntity GetOrNew(Entity ent) {
		if(ent instanceof LivingEntity) return WrappedLivingEntity.GetOrNew((LivingEntity)ent);
		else {
			//Try to find existing instance
			WrappedEntity p = Get(ent);
			if(p != null) return p;
			
			//None exists, instantiate
			else return UnitylikeEnvironment.GetInstance().Instantiate(new WrappedEntity(ent));
		}
	}
	
	public static WrappedEntity Get(Entity ent) {
		for(WrappedEntity i : UnitylikeEnvironment.GetInstance().FindObjectsOfType(WrappedEntity.class)) if(i.entity.equals(ent)) return i;
		return null;
	}
	
}
