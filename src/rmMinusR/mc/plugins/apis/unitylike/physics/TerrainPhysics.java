package rmMinusR.mc.plugins.apis.unitylike.physics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;

public final class TerrainPhysics {

	public Chunk chunk;
	
	public static final BlockFace[] ordinals = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN };
	
	public TerrainPhysics(Chunk chunk) {
		this.chunk = chunk;
	}
	
	private static Vector3 BlockClamp(Vector3 global_pos, BlockVector3 block_pos) {
		return new Vector3(
					MathfClamp(global_pos.x, block_pos.x, block_pos.x+1),
					MathfClamp(global_pos.y, block_pos.y, block_pos.y+1),
					MathfClamp(global_pos.z, block_pos.z, block_pos.z+1)
				);
	}
	
	private static float MathfClamp(float x, float a, float b) {
		float min = a<b ? a : b;
		float max = a>b ? a : b;
		if(x < min) return min;
		if(x > max) return max;
		return x;
	}
	
	private Block SafeAccessGlobal(BlockVector3 v) { return SafeAccessGlobal(v.GetXWithinChunk(), (int)v.y, v.GetZWithinChunk()); }
	private Block SafeAccessGlobal(int x, int y, int z) {
		if(0 < x && x <= 15
		&& 0 < z && z <= 15
		&& 0 < y && y <= chunk.getWorld().getMaxHeight())
			return chunk.getBlock(x, y, z);
		else
			return null;
	}
	
	private static final Function<Block, Boolean> defaultSelector = new Function<Block, Boolean>() {
		@Override
		public Boolean apply(Block t) { return t.isEmpty() || t.isPassable(); }
	};
	
	public Vector3 GetClosestPoint(final Vector3 global_pos, int max_search) { return GetClosestPoint(global_pos, max_search, defaultSelector); }
	public Vector3 GetClosestPoint(final Vector3 global_pos, int max_search, Function<Block, Boolean> selector) {
		ArrayList<Block> relative_search = new ArrayList<Block>();
		
		for(int x = -max_search; x <= max_search; x++) for(int y = -max_search; y <= max_search; y++) for(int z = -max_search; z <= max_search; z++) {
			Vector3 rv = new Vector3(x, y, z);
			BlockVector3 bv = global_pos.Add(rv).ToBlockVector3();
			Block b = SafeAccessGlobal(bv);
			if(b != null) relative_search.add(b);
		}
		
		relative_search.sort(new Comparator<Block>() {
			@Override
			public int compare(Block a, Block b) {
				Vector3 ca = new BlockVector3(a).GetCenterOfBlock();
				Vector3 cb = new BlockVector3(b).GetCenterOfBlock();
				float da = Vector3.Distance(global_pos, ca);
				float db = Vector3.Distance(global_pos, cb);
				if(da < db) return -1;
				if(da > db) return 1;
				return 0;
			}
		});
		
		for(Block b : relative_search) {
			if(!b.isPassable()) return BlockClamp(global_pos, new BlockVector3(b));
		}
		
		return null;
	}
	
}
