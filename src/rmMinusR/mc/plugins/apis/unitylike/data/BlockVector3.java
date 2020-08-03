package rmMinusR.mc.plugins.apis.unitylike.data;

import com.comphenix.protocol.wrappers.ChunkCoordIntPair;

import org.bukkit.Location;
import org.bukkit.block.Block;

public final class BlockVector3 {
	
	public long x, y, z;
	
	public BlockVector3(long x, long y, long z) { this.x = x; this.y = y; this.z = z; }
	public BlockVector3(Vector3 fpos) { this((long)fpos.x, (long)fpos.y, (long)fpos.z); }
	public BlockVector3(Location loc) { this((long)loc.getX(), (long)loc.getY(), (long)loc.getZ()); }
	public BlockVector3(Block b) { this(b.getLocation()); }
	
	public Vector3 GetCenterOfBlock() { return new Vector3(x+0.5f, y+0.5f, z+0.5f); }
	
	public int GetChunkX() { return (int) Math.floor(x/16f); }
	public int GetChunkZ() { return (int) Math.floor(z/16f); }
	public ChunkCoordIntPair GetChunk() { return new ChunkCoordIntPair(GetChunkX(), GetChunkZ()); }
	
	public int GetXWithinChunk() { return (int)(x - GetChunkX()*16); }
	public int GetZWithinChunk() { return (int)(z - GetChunkZ()*16); }
	public BlockVector3 GetWithinChunk() { return new BlockVector3(GetXWithinChunk(), y, GetZWithinChunk()); }
}
