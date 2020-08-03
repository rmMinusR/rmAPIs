package rmMinusR.mc.plugins.apis.simpleillusion;

import java.util.HashMap;
import java.util.UUID;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.simpleillusion.IllusoryOverlay.ActionPolicy;

public class IllusionManager implements Listener {
	
	public HashMap<UUID,IllusoryOverlay> illusions;
	
	public IllusionPacketInterceptorSingle interceptorSingle;
	public IllusionPacketInterceptorMulti interceptorMulti;
	
	public IllusionManager() {
		illusions = new HashMap<UUID, IllusoryOverlay>();
		interceptorSingle = new IllusionPacketInterceptorSingle();
		interceptorMulti = new IllusionPacketInterceptorMulti();
	}
	
	public void OnEnable() {
		for(Player p : Bukkit.getOnlinePlayers()) GetIllusoryWorld(p);
		
		ProtocolLibrary.getProtocolManager().addPacketListener(interceptorSingle);
		ProtocolLibrary.getProtocolManager().addPacketListener(interceptorMulti);
		
		tickerTaskID = Bukkit.getScheduler().runTaskTimer(RmApisPlugin.INSTANCE, ticker, 1, 1).getTaskId();
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
	}
	
	public void OnDisable() {
		for(Player p : Bukkit.getOnlinePlayers()) GetIllusoryWorld(p).ShowReality();
		illusions.clear();
		
		ProtocolLibrary.getProtocolManager().removePacketListener(interceptorSingle);
		ProtocolLibrary.getProtocolManager().removePacketListener(interceptorMulti);
		interceptorSingle = null;
		interceptorMulti = null;
		
		Bukkit.getScheduler().cancelTask(tickerTaskID);
		HandlerList.unregisterAll(this);
	}
	
	public IllusoryOverlay GetIllusoryWorld(Player player) {
		if(!illusions.containsKey(player.getUniqueId())) illusions.put(player.getUniqueId(), new IllusoryOverlay(player));
		
		return illusions.get(player.getUniqueId());
	}
	
	private int tickerTaskID;
	private final Runnable ticker = new Runnable() {
		
		@Override
		public void run() {
			OnTick();
		}
	};
	public void OnTick() {
		
		for(Player p : Bukkit.getOnlinePlayers()) GetIllusoryWorld(p).Update();
		
	}
	
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event) {
		GetIllusoryWorld(event.getPlayer());
	}
	
	@EventHandler
	public void OnPlayerLeave(PlayerQuitEvent event) {
		illusions.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void OnPlayerBreak(BlockBreakEvent event) {
		BlockPosition pos = new BlockPosition(event.getBlock().getLocation().toVector());
		IllusoryOverlay iw = GetIllusoryWorld(event.getPlayer());
		
		IllusoryOverlay.IllusionBlock ib = iw.GetIllusionBlock(event.getBlock().getWorld(), pos);
		if(ib != null) {
			
			if(ib.onBreak == ActionPolicy.ShowReality) iw.ShowReality(ib);
			
			if(ib.onBreak == ActionPolicy.Cancel) event.setCancelled(true);
			
		}
	}
	
	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event) {
		if(event.getClickedBlock() == null) return;
		
		BlockPosition pos = new BlockPosition(event.getClickedBlock().getLocation().toVector());
		IllusoryOverlay iw = GetIllusoryWorld(event.getPlayer());
		
		IllusoryOverlay.IllusionBlock ib = iw.GetIllusionBlock(event.getClickedBlock().getWorld(), pos);
		
		if(ib != null) {
			
			if(ib.onInteract == ActionPolicy.ShowReality) iw.ShowReality(ib);
			
			if(ib.onInteract == ActionPolicy.Cancel) event.setCancelled(true);
			
		}
	}
	
	public class IllusionPacketInterceptorSingle extends PacketAdapter {
		
		public IllusionPacketInterceptorSingle() {
			super(RmApisPlugin.INSTANCE, ListenerPriority.NORMAL, PacketType.Play.Server.BLOCK_CHANGE);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
				PacketContainer packet = event.getPacket();
				WrapperPlayServerBlockChange wrapped = new WrapperPlayServerBlockChange(packet);
				IllusoryOverlay iw = GetIllusoryWorld(event.getPlayer());
				
				IllusoryOverlay.IllusionBlock ib = iw.GetIllusionBlock(event.getPlayer().getWorld(), wrapped.getLocation());
				if(ib != null) {
					wrapped.setBlockData(ib.AsWrappedBlockData());
				}
			}
		}
	}
	
	public class IllusionPacketInterceptorMulti extends PacketAdapter {
		
		public IllusionPacketInterceptorMulti() {
			super(RmApisPlugin.INSTANCE, ListenerPriority.NORMAL, PacketType.Play.Server.MULTI_BLOCK_CHANGE);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
				PacketContainer packet = event.getPacket();
				WrapperPlayServerMultiBlockChange wrapped = new WrapperPlayServerMultiBlockChange(packet);
				IllusoryOverlay iw = GetIllusoryWorld(event.getPlayer());
				
				for(MultiBlockChangeInfo i : wrapped.getRecords()) {
					
					IllusoryOverlay.IllusionBlock ib = iw.GetIllusionBlock(event.getPlayer().getWorld(), new BlockPosition(i.getAbsoluteX(), i.getY(), i.getAbsoluteZ()));
					if(ib != null) {
						i.setData(ib.AsWrappedBlockData());
					}
					
				}
			}
		}
	}
	/* //TODO implement chunk packet interceptor
	public class IllusionPacketInterceptorChunk extends PacketAdapter {
		
		public IllusionPacketInterceptorChunk() {
			super(RmApisPlugin.INSTANCE, ListenerPriority.NORMAL, PacketType.Play.Server.CHUNK_DATA);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {
				PacketContainer packet = event.getPacket();
				WrapperPlayServerMultiBlockChange wrapped = new WrapperPlayServerMultiBlockChange(packet);
				IllusoryWorld iw = GetIllusoryWorld(event.getPlayer());
				
				for(MultiBlockChangeInfo i : wrapped.getRecords()) {
					
					IllusoryWorld.IllusionBlock ib = iw.GetIllusionBlock(event.getPlayer().getWorld(), new BlockPosition(i.getAbsoluteX(), i.getY(), i.getAbsoluteZ()));
					if(ib != null) {
						i.setData(ib.AsWrappedBlockData());
					}
					
				}
			}
		}
	}
	//*/
}
