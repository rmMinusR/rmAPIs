package rmMinusR.mc.plugins.apis.debug;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTCompound;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem;
import rmMinusR.mc.plugins.apis.forgelike.CustomMaterial;
import rmMinusR.mc.plugins.apis.particle.AdvancedParticleTemplate;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.Physics;
import rmMinusR.mc.plugins.apis.unitylike.physics.RaycastHit;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedLivingEntity;

public class RodOfRaycasting extends CustomItem {
	
	public static final String ID = DebugItemNamespace.namespacePrefix+"raycast_rod";
	
	public RodOfRaycasting(ItemStack ref, NBTCompound data) {
		super(ref, data);
	}
	
	@Override
	public boolean OnRightClick(LivingEntity holder) {
		WrappedLivingEntity wrappedHolder = WrappedLivingEntity.GetOrNew(holder);
		Vector3 pos = new Vector3(holder.getEyeLocation());
		Vector3 look_vec = wrappedHolder.GetTransform().Forward();
		
		float dist = 25;
		
		RaycastHit[] allHits = Physics.RaycastAll(holder.getWorld(), pos, look_vec, dist, x -> x.gameObject != wrappedHolder);
		RaycastHit hit = allHits.length > 0 ? allHits[0] : null;
		
		if(hit != null) {
			ParticleGraphics.drawDebugCross(holder.getWorld(), hit.point, 1);
			
			ParticleGraphics.drawLine(
						holder.getWorld(),
						hit.point.ToBukkit(),
						hit.point.Add(hit.normal).ToBukkit(),
						new AdvancedParticleTemplate(Particle.FIREWORKS_SPARK).setVelocity(hit.normal.Mul(0.5f).ToBukkit()),
						0.1f
					);
		}
		
		holder.sendMessage(Debug.MakeNullSafe(hit));

		return false;
	}
	
	@Override
	public boolean OnLeftClick(LivingEntity holder) {
		WrappedLivingEntity wrappedHolder = WrappedLivingEntity.GetOrNew(holder);
		Vector3 pos = new Vector3(holder.getEyeLocation());
		Vector3 look_vec = wrappedHolder.GetTransform().Forward();
		
		RaycastHit[] allHits = Physics.RaycastAll(holder.getWorld(), pos, look_vec, 25, x -> x.gameObject != wrappedHolder);
		RaycastHit hit = allHits.length > 0 ? allHits[0] : null;
		
		if(hit != null && hit.collider != null) {
			hit.collider.DebugRender();
		}
		
		return false;
	}
	
	@Override
	public CustomMaterial GetMaterial() {
		return CustomMaterial.GetOrInstantiate(ID, RodOfRaycasting.class);
	}

	@Override
	public ItemStack GetRenderType() {
		ItemStack is = new ItemStack(Material.BLAZE_ROD);
		ItemMeta im = is.getItemMeta();
		im.setLocalizedName(DebugItemNamespace.LocalizeToTechnical(ID));
		is.setItemMeta(im);
		return is;
	}

	public static void Register() {
		CustomMaterial.GetOrInstantiate(ID, RodOfRaycasting.class);
	}
	
}
