package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.ArrayList;

import org.bukkit.entity.Entity;

import rmMinusR.mc.plugins.apis.unitylike.data.Transform;

public class WrappedEntity extends GameObject {
	
	public Entity entity;
	
	protected WrappedEntity(Entity entity) {
		this.entity = entity;
	}
	
	@Override
	public Transform GetTransform() { return new Transform(entity.getLocation()); }
	@Override
	public void SetTransform(Transform t) { t.WriteTo(entity.getLocation()); }
	
	@Override
	public void AddComponent(Component c) {
		//Only allow non-Transform components
		if(Transform.class.isAssignableFrom(c.getClass())) throw new IllegalArgumentException("Cannot add a Transform to WrappedEntity");
		super.AddComponent(c);
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
			return out;
		}
		return super.GetComponents(clazz);
	}
	
}
