package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import java.util.ArrayList;

import org.bukkit.entity.Entity;

import rmMinusR.mc.plugins.apis.unitylike.core.Component;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;

public class WrappedEntity extends GameObject {
	
	public Entity entity;
	
	public WrappedEntity(Entity entity) {
		super(entity.getWorld());
		this.entity = entity;
		
		AddComponent(new EntityCollider(this));
	}
	
	@Override
	public Transform GetTransform() { return new Transform(entity.getLocation()); }
	@Override
	public void SetTransform(Transform t) { t.WriteTo(entity.getLocation()); }
	
	@Override
	public void AddComponent(Component c, boolean doAwake) {
		//Only allow non-Transform components
		if(Transform.class.isAssignableFrom(c.getClass())) throw new IllegalArgumentException("Cannot add a Transform to WrappedEntity");
		super.AddComponent(c, doAwake);
	}
	
	@Override
	public Component GetComponent(Class<? extends Component> clazz) {
		if(clazz.isAssignableFrom(Transform.class)) return GetTransform();
		return super.GetComponent(clazz);
	}

	@Override
	public ArrayList<Component> GetComponents(Class<? extends Component> clazz) {
		if(clazz.isAssignableFrom(Transform.class)) {
			ArrayList<Component> out = new ArrayList<Component>();
			out.add(GetTransform());
			out.addAll(super.GetComponents(clazz));
			return out;
		}
		return super.GetComponents(clazz);
	}
	
	@Override
	public String toString() {
		return "Wrapped entity: "+entity.toString();
	}
	
}
