package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.entity.LivingEntity;

public class WrappedLivingEntity extends WrappedEntity {
	
	public LivingEntity livingEntity;
	
	public WrappedLivingEntity(LivingEntity livingEntity) {
		super(livingEntity);
		this.livingEntity = livingEntity;
	}
	
}
