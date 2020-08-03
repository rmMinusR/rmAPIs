package rmMinusR.mc.plugins.apis.unitylike.core;

import org.bukkit.entity.LivingEntity;

public class WrappedLivingEntity extends WrappedEntity {
	
	public LivingEntity livingEntity;
	
	protected WrappedLivingEntity(LivingEntity livingEntity) {
		super(livingEntity);
		this.livingEntity = livingEntity;
	}
	
}
