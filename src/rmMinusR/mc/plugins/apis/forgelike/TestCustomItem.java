package rmMinusR.mc.plugins.apis.forgelike;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import de.tr7zw.nbtapi.NBTCompound;
import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.particle.AdvancedParticleTemplate;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.unitylike.core.Time;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.BoxCollider;
import rmMinusR.mc.plugins.apis.unitylike.physics.Physics;
import rmMinusR.mc.plugins.apis.unitylike.physics.RaycastHit;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedLivingEntity;

public class TestCustomItem extends CustomItem {
	
	public static final String ID = "customitem:debug";
	
	public TestCustomItem(ItemStack ref, NBTCompound data) {
		super(ref, data);
	}
	
	@Override
	public void OnTick(Context context, LivingEntity holder) {
		
	}
	
	@Override
	public boolean OnRightClick(LivingEntity holder) {
		WrappedLivingEntity wrappedHolder = (WrappedLivingEntity) RmApisPlugin.INSTANCE.unitylikeEnv.Wrap(holder);
		Vector3 pos = new Vector3(holder.getEyeLocation());
		Vector3 look_vec = wrappedHolder.GetTransform().forward();
		
		float dist = 25;
		
		RaycastHit[] allHits = Physics.RaycastAll(holder.getWorld(), pos, look_vec, dist, x -> x.gameObject != wrappedHolder);
		RaycastHit hit = allHits.length > 0 ? allHits[0] : null;
		
		if(hit != null) {
			
			BoxCollider coll = (BoxCollider)hit.collider;
			Vector3 collCenter = coll.gameObject.GetTransform().GetLocalToWorldMatrix().TransformPoint(coll.local_offset);
			
			ParticleGraphics.wireCube(
						holder.getWorld(),
						collCenter.Sub(coll.size.Mul(0.51f)).ToBukkit(),
						collCenter.Add(coll.size.Mul(0.51f)).ToBukkit(),
						new AdvancedParticleTemplate(Particle.END_ROD),
						0.1f
					);
			
			ParticleGraphics.drawDebugCross(holder.getWorld(), hit.point, 1);
			
			ParticleGraphics.drawLine(
						holder.getWorld(),
						hit.point.ToBukkit(),
						hit.point.Add(hit.normal).ToBukkit(),
						new AdvancedParticleTemplate(Particle.FIREWORKS_SPARK).setVelocity(hit.normal.ToBukkit()),
						0.1f
					);
			
			ParticleGraphics.drawLine(
						holder.getWorld(),
						hit.point.ToBukkit(),
						hit.point.Sub(hit.normal).ToBukkit(),
						new AdvancedParticleTemplate(Particle.FIREWORKS_SPARK).setVelocity(hit.normal.Mul(-1).ToBukkit()),
						0.1f
					);
		}
		
		holder.sendMessage("["+Time.time+"] "+hit);

		return false;
	}
	
	@Override
	public boolean OnLeftClick(LivingEntity holder) {
		WrappedLivingEntity wrappedHolder = (WrappedLivingEntity) RmApisPlugin.INSTANCE.unitylikeEnv.Wrap(holder);
		Vector3 pos = new Vector3(holder.getEyeLocation());
		Vector3 look_vec = wrappedHolder.GetTransform().forward();
		
		RaycastHit[] allHits = Physics.RaycastAll(holder.getWorld(), pos, look_vec, 25, x -> x.gameObject != wrappedHolder);
		RaycastHit hit = allHits.length > 0 ? allHits[0] : null;
		
		if(hit != null && hit.collider != null) {
			hit.collider.DebugRender();
		}
		
		return false;
	}
	
	@Override
	public CustomMaterial GetMaterial() {
		return CustomMaterial.GetOrInstantiate(ID, getClass());
	}
	
}
