package rmMinusR.mc.plugins.apis.simpleillusion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMultiBlockChange;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class IllusoryOverlay {
	public static ProtocolManager protocolManager;
	
	public final Player owner;
	
	private ArrayList<IllusionBlock> shown;
	
	private ArrayList<IllusionBlock> queue;
	
	public IllusoryOverlay(Player owner) {
		if(protocolManager == null) protocolManager = ProtocolLibrary.getProtocolManager();
		
		this.owner = owner;
		shown = new ArrayList<IllusionBlock>();
		queue = new ArrayList<IllusionBlock>();
	}
	
	public IllusionBlock QueueIllusionBlock(World w, BlockPosition pos, Material m, int data, IIllusionSource source) {
		IllusionBlock tgt = GetIllusionBlock(w, pos);
		
		if(tgt == null) {
			tgt = new IllusionBlock(w, pos, m, data, source);
			queue.add(tgt);
		} else {
			tgt.world = w;
			tgt.position = pos;
			tgt.material = m;
			tgt.data = data;
			tgt.source = source;
		}
		
		return tgt;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(owner.isOnline()) ShowReality();
		super.finalize();
	}
	
	public void Update() {
		if(queue.size() > 0) RenderQueue();
	}
	
	public void RenderQueue() {
		queue.removeAll(shown);
		SendSafe(queue);
		shown.addAll(queue);
		queue.clear();
	}
	
	public void Rerender() {
		SendSafe(shown);
	}
	
	public void Rerender(World world, Chunk chunk) { Rerender(world, new ChunkCoordIntPair(chunk.getX(), chunk.getZ())); }
	public void Rerender(World w, ChunkCoordIntPair chunk) {
		HashMap<ChunkCoordIntPair,ArrayList<IllusionBlock>> chunkGrouped = GroupByChunk(shown);
		
		if(chunkGrouped.containsKey(chunk)) SendSafe(chunkGrouped.get(chunk));
	}
	
	public void Rerender(World w, BlockPosition pos) {
		IllusionBlock tgt = GetIllusionBlock(w, pos);
		if(tgt == null) return;
		
		SendSafe(tgt);
	}

	public IllusionBlock GetIllusionBlock(World w, BlockPosition pos) {
		for(IllusionBlock b : shown) if(b.position.equals(pos) && b.world.getUID() == w.getUID()) return b;
		return null;
	}
	
	public void ShowReality() {
		ArrayList<IllusionBlock> tmp = new ArrayList<IllusionBlock>(); tmp.addAll(shown);
		
		for(IllusionBlock i : tmp) ShowReality(i);
	}
	
	public void ShowReality(World w, BlockPosition pos) {
		IllusionBlock tgt = GetIllusionBlock(w, pos);
		if(tgt == null) return;
		
		ShowReality(tgt);
	}
	
	public void ShowReality(IllusionBlock tgt) {
		shown.remove(tgt);
		queue.remove(tgt);
		
		SendSafe(tgt.GetReality());
	}
	
	private void SendSafe(ArrayList<IllusionBlock> tgt) {
		ArrayList<IllusionBlock> tmp = new ArrayList<IllusionBlock>(tgt);
		Bukkit.getScheduler().scheduleSyncDelayedTask(RmApisPlugin.INSTANCE, new Runnable() {
			@Override
			public void run() {
				try {
					SendImmediate(tmp);
				} catch(Throwable t) {
					RmApisPlugin.INSTANCE.logger.warning("An error occurred while sending multiblock illusion:");
					t.printStackTrace();
				}
			}
		});
	}
	
	private void SendSafe(IllusionBlock tgt) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(RmApisPlugin.INSTANCE, new Runnable() {
			@Override
			public void run() {
				try {
					SendImmediate(tgt);
				} catch(Throwable t) {
					RmApisPlugin.INSTANCE.logger.warning("An error occurred while sending single-block illusion:");
					t.printStackTrace();
				}
			}
		});
	}
	
	private void SendImmediate(ArrayList<IllusionBlock> tgt) {
		if(tgt == null || tgt.size() == 0) return;
		if(tgt.size() == 1) SendImmediate(tgt.get(0));
		

		HashMap<ChunkCoordIntPair,ArrayList<IllusionBlock>> chunkGrouped = GroupByChunk(tgt);
		
		if(chunkGrouped.size() > 1) {
			for(Entry<ChunkCoordIntPair,ArrayList<IllusionBlock>> b : chunkGrouped.entrySet()) {
				SendImmediate(b.getValue());
			}
		} else if(chunkGrouped.size() == 1) {
			WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange();
			
			@SuppressWarnings("unchecked")
			Entry<ChunkCoordIntPair,ArrayList<IllusionBlock>> data = (Entry<ChunkCoordIntPair,ArrayList<IllusionBlock>>)chunkGrouped.entrySet().toArray()[0];
			
			MultiBlockChangeInfo[] changes = new MultiBlockChangeInfo[data.getValue().size()];
			for(int i = 0; i < changes.length; i++) {
				IllusionBlock ib = data.getValue().get(i);
				//I don't know why. I don't want to know why. I shouldn't
				//have to wonder why. But for whatever reason MBCI's coordinates
				//aren't encoded correctly when using org.bukkit.Location
				BlockPosition relPos = ib.GetPositionWithinChunk(); //Position relative to chunk
				MultiBlockChangeInfo info = new MultiBlockChangeInfo(
						//Format: 0xX Z YY    0xXXXX ZZZZ YYYYYYYY
						(short) ( (relPos.getY() & 0x00FF) | (relPos.getZ() << 8 & 0x0F00) | (relPos.getX() << 12 & 0xF000) ),
						ib.AsWrappedBlockData(),
						ib.GetChunk()
					);
				changes[i] = info;
			}
			
			packet.setChunk(data.getKey());
			packet.setRecords(changes);
			packet.sendPacket(owner);
		} else {
			//Nothing to render
		}
	}
	
	private void SendImmediate(IllusionBlock tgt) {
		if(tgt == null) return;
		
		WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange();
		
		packet.setLocation(tgt.position);
		//Necessary although interceptor overwrites this data anyway
		packet.setBlockData(tgt.AsWrappedBlockData());
		
		packet.sendPacket(owner);
	}
	
	private HashMap<ChunkCoordIntPair,ArrayList<IllusionBlock>> GroupByChunk(ArrayList<IllusionBlock> ungrouped) {
		HashMap<ChunkCoordIntPair,ArrayList<IllusionBlock>> grouped = new HashMap<ChunkCoordIntPair, ArrayList<IllusionBlock>>();

		for(IllusionBlock b : ungrouped) {
			
			if(!grouped.containsKey(b.GetChunk())) grouped.put(b.GetChunk(), new ArrayList<IllusionBlock>());
			
			grouped.get(b.GetChunk()).add(b);
			
		}
		
		return grouped;
	}
	
	public interface IIllusionSource { }
	
	public static class IllusionBlock {
		public World world;
		public BlockPosition position;
		
		public Material material;
		public int data;
		
		public IIllusionSource source;
		public ActionPolicy onBreak;
		public ActionPolicy onInteract;
		
		public IllusionBlock(World world, BlockPosition pos, Material material, int data, IIllusionSource source) {
			this.world = world;
			this.position = pos;
			this.material = material;
			this.data = data;
			this.source = source;
			
			onBreak = ActionPolicy.Cancel;
			onInteract = ActionPolicy.Passthrough;
		}

		public IllusionBlock SetAllActions(ActionPolicy policy) {
			onBreak = policy;
			onInteract = policy;
			return this;
		}
		
		public IllusionBlock SetBreakAction(ActionPolicy policy) {
			onBreak = policy;
			return this;
		}
		
		public IllusionBlock SetInteractAction(ActionPolicy policy) {
			onInteract = policy;
			return this;
		}
		
		public ChunkCoordIntPair GetChunk() {
			return new ChunkCoordIntPair(
						(int) Math.floor(position.getX()/16f),
						(int) Math.floor(position.getZ()/16f)
					);
		}
		
		@SuppressWarnings("deprecation")
		public IllusionBlock GetReality() {
			return new IllusionBlock(
						world, position,
						world.getBlockAt(position.toLocation(world)).getType(), world.getBlockAt(position.toLocation(world)).getData(),
						null
					);
		}
		
		public BlockPosition GetPositionWithinChunk() {
			return new BlockPosition(position.getX() & 15, position.getY(), position.getZ() & 15);
		}
		
		@SuppressWarnings("deprecation")
		public WrappedBlockData AsWrappedBlockData() {
			return WrappedBlockData.createData(material, data);
		}
	}
	
	public static enum ActionPolicy {
		Cancel, Passthrough, ShowReality
	}
	
	/*
	
	public static abstract class Illusion {
		public World world;
		
		public static ProtocolManager protocolManager;
		
		public Illusion(World w) {
			world = w;
			if(protocolManager == null) protocolManager = ProtocolLibrary.getProtocolManager();
		}
		
		public abstract void Hide();
		public abstract void Show(Player target);
	}
	
	public static class IllusionBlock extends Illusion {

		public Block block;
		
		public IllusionBlock(World w, Block block) {
			super(w);
			this.block = block;
		}

		@Override
		public void Hide() {
			
		}

		@Override
		public void Show(Player target) {
			PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
			
		}
		
	}
	
	// */
}
