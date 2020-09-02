package rmMinusR.mc.plugins.apis.unitylike.data;

import com.comphenix.protocol.wrappers.ChunkCoordIntPair;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BlockVector3 implements Cloneable {
	
	//Fields
	public long x, y, z;
	
	//Quickref
	public static Vector3    zero() { return new Vector3( 0,  0,  0); }
	public static Vector3     one() { return new Vector3( 1,  1,  1); }
	
	public static Vector3   right() { return new Vector3( 1,  0,  0); }
	public static Vector3      up() { return new Vector3( 0,  1,  0); }
	public static Vector3 forward() { return new Vector3( 0,  0,  1); }
	
	public static Vector3    left() { return new Vector3(-1,  0,  0); }
	public static Vector3    down() { return new Vector3( 0, -1,  0); }
	public static Vector3    back() { return new Vector3( 0,  0, -1); }

	//Ctors
	public BlockVector3(long x, long y, long z) { this.x = x; this.y = y; this.z = z; }
	public BlockVector3(float x, float y, float z) { this((long)Mathf.Floor(x), (long)Mathf.Floor(y), (long)Mathf.Floor(z)); }
	public BlockVector3(Vector3 fpos) { this(fpos.x, fpos.y, fpos.z); }
	public BlockVector3(Location loc) { this((float)loc.getX(), (float)loc.getY(), (float)loc.getZ()); }
	public BlockVector3(Block b) { this(b.getLocation()); }
	
	//Data IO
	public Vector3 GetCenterOfBlock() { return new Vector3(x+0.5f, y+0.5f, z+0.5f); }
	
	public int GetChunkX() { return (int) Math.floor(x/16f); }
	public int GetChunkZ() { return (int) Math.floor(z/16f); }
	public ChunkCoordIntPair GetChunk() { return new ChunkCoordIntPair(GetChunkX(), GetChunkZ()); }
	
	public int GetXWithinChunk() { return (int)(x - GetChunkX()*16); }
	public int GetZWithinChunk() { return (int)(z - GetChunkZ()*16); }
	public BlockVector3 GetWithinChunk() { return new BlockVector3(GetXWithinChunk(), y, GetZWithinChunk()); }
	
	public Block Fetch(World terrain) {
		if(0 <= y && y < terrain.getMaxHeight() && terrain.isChunkGenerated(GetChunkX(), GetChunkZ()))
			return terrain.getBlockAt((int)x, (int)y, (int)z);
		else return null;
	}
	
	@Override
	protected BlockVector3 clone() {
		return new BlockVector3(x, y, z);
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %d, %d)", x, y, z);
	}
	
	//Math
	public BlockVector3 Add(BlockVector3 other) { return Add(this, other); }
	public static BlockVector3 Add(BlockVector3 a, BlockVector3... etc) {
		if(etc.length == 0) {
			return a.clone();
		} else if(etc.length == 1) {
			return new BlockVector3(a.x+etc[0].x, a.y+etc[0].y, a.z+etc[0].z);
		} else {
			BlockVector3[] etc2 = new BlockVector3[etc.length-1];
			for(int i = 0; i < etc2.length; i++) etc2[i] = etc[i+1];
			return Add(a, Add(etc[0], etc2));
		}
	}
	
	public BlockVector3 Sub(BlockVector3 other) { return Sub(this, other); }
	public static BlockVector3 Sub(BlockVector3 a, BlockVector3 b) { return new BlockVector3(a.x-b.x, a.y-b.y, a.z-b.z); }
	
	public float DistanceTo(Vector3 other) {
		return GetCenterOfBlock().Distance(other);
	}
}
