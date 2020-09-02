package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import rmMinusR.mc.plugins.apis.unitylike.core.UnitylikeEnvironment;

public class WrappedLivingEntity extends WrappedEntity {
	
	public LivingEntity livingEntity;
	
	public WrappedLivingEntity(LivingEntity livingEntity) {
		super(livingEntity);
		this.livingEntity = livingEntity;
	}
	
	public static WrappedLivingEntity GetOrNew(LivingEntity ent) {
		if(ent instanceof Player) return WrappedPlayer.GetOrNew((Player)ent);
		else {
			//Try to find existing instance
			WrappedLivingEntity p = Get(ent);
			if(p != null) return p;
			
			//None exists, instantiate
			else return UnitylikeEnvironment.GetInstance().Instantiate(new WrappedLivingEntity(ent));
		}
	}
	
	public static WrappedLivingEntity Get(LivingEntity ent) {
		for(WrappedLivingEntity i : UnitylikeEnvironment.GetInstance().FindObjectsOfType(WrappedLivingEntity.class)) if(i.entity.equals(ent)) return i;
		return null;
	}
	
}
