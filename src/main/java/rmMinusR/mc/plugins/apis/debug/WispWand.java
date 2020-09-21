package rmMinusR.mc.plugins.apis.debug;

import java.util.*;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.nbtapi.NBTCompound;
import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.armorstand.VirtualFloatingHead;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem;
import rmMinusR.mc.plugins.apis.forgelike.CustomMaterial;
import rmMinusR.mc.plugins.apis.particle.AdvancedParticleTemplate;
import rmMinusR.mc.plugins.apis.particle.ParticleGraphics;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.core.IPersistentSerializable;
import rmMinusR.mc.plugins.apis.unitylike.core.IRenderable;
import rmMinusR.mc.plugins.apis.unitylike.core.RenderDelegate;
import rmMinusR.mc.plugins.apis.unitylike.core.Scene;
import rmMinusR.mc.plugins.apis.unitylike.core.Time;
import rmMinusR.mc.plugins.apis.unitylike.data.Random;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedLivingEntity;

public class WispWand extends CustomItem {
	
	public static final String ID = DebugItemNamespace.namespacePrefix+"object_wand";
	
	public WispWand(ItemStack ref, NBTCompound data) {
		super(ref, data);
	}

	private ASPoseWatcher watcher = null;
	@Override
	public boolean OnLeftClick(LivingEntity holder) {
		if(watcher == null) {
			holder.sendMessage("Watching EntityMetadata packets...");
			watcher = new ASPoseWatcher();
			ProtocolLibrary.getProtocolManager().addPacketListener(watcher);
		}

		return false;
	}

	private static final class ASPoseWatcher extends PacketAdapter {

		public ASPoseWatcher() {
			super(RmApisPlugin.INSTANCE, ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_METADATA);
		}

		@Override
		public void onPacketSending(PacketEvent event) {
			WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event.getPacket());

			Debug.Log("EID: "+packet.getEntityID());
			List<WrappedWatchableObject> sorted = new ArrayList<WrappedWatchableObject>(packet.getMetadata());
			sorted.sort(Comparator.comparingInt(WrappedWatchableObject::getIndex));
			for(WrappedWatchableObject o : sorted) {
				if(o.getValue() instanceof Vector3F) {
					Vector3F v = (Vector3F) o.getValue();
					Debug.Log(o.getIndex()+": Vector3F:{"+v.getX()+","+v.getY()+","+v.getZ()+"}");
				}
				else Debug.Log(o.getIndex()+": "+o.getValue().getClass()+" "+o.getValue());
			}
			Debug.Log("============");
		}
	}

	@Override
	public boolean OnRightClick(LivingEntity holder) {
		Transform tf_holder = WrappedLivingEntity.GetOrNew(holder).GetTransform();
		Vector3 spawnloc = tf_holder.GetPosition() + tf_holder.forward()*4; //FIXME doesn't actually work
		Scene s = Scene.GetOrNew(holder.getWorld());
		s.Instantiate(new Wisp(holder.getWorld(), spawnloc));
		
		return false;
	}
	
	@Override
	public CustomMaterial GetMaterial() {
		return CustomMaterial.GetOrInstantiate(ID, WispWand.class);
	}
	
	public static void Register() {
		CustomMaterial.GetOrInstantiate(ID, WispWand.class);
	}
	
	@Override
	public ItemStack GetRenderType() {
		ItemStack is = new ItemStack(Material.BELL);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Wand of Wisps");
		is.setItemMeta(im);
		return is;
	}
	
	public static final class Wisp extends GameObject implements IRenderable, IPersistentSerializable {
		
		public Vector3 velocity;

		public Wisp(World world, Vector3 pos) {
			super(world);
			velocity = Vector3.zero();
			GetComponent(Transform.class).SetPosition(pos);
		}
		
		private double t_nextJump;
		public void Update() {
			if(Time.Until(t_nextJump) <= 0) {
				t_nextJump = Time.time + Random.Range(5d, 10d);
				
				GetComponent(Transform.class).SetPosition( GetComponent(Transform.class).GetPosition().Add(Random.InsideUnitSphere().Mul(3)) );
			}
			
			super.Update();
		}
		
		@Override
		public void PhysicsUpdate() {
			velocity = velocity.Add(Random.InsideUnitSphere().Mul(1f)).WithBoundedMagnitude(1, 2);
			GetTransform().SetPosition(GetTransform().GetPosition().Add(velocity.Mul((float) Time.deltaTime)));
			
			super.PhysicsUpdate();
		}

		private VirtualFloatingHead _renderer;
		@Override
		public Collection<RenderDelegate> Render() {
			if(_renderer == null) {
				_renderer = new VirtualFloatingHead(this, GetComponent(Transform.class));
				_renderer.SetItem(new ItemStack(Material.DIAMOND_HELMET, 1));
			}

			Set<RenderDelegate> out = new HashSet<RenderDelegate>(1);
			out.add(_renderer);
			return out;
		}
		
		public static final class WispRenderer extends RenderDelegate {

			public WispRenderer(Wisp owner) {
				super(owner);
			}

			@Override
			public int GetPriority() {
				return 0;
			}

			@Override
			protected void Render() {
				Wisp w = (Wisp)owner;
				ParticleGraphics.surfCube(
						w.scene.ref,
						(w.GetTransform().GetPosition() + Vector3.one() * -0.2f).ToBukkit(),
						(w.GetTransform().GetPosition() + Vector3.one() *  0.2f).ToBukkit(),
						new AdvancedParticleTemplate(Particle.REDSTONE).setColor(255, 127, 0),
						0.07f);
			}
			
		}
		
	}
	
}
