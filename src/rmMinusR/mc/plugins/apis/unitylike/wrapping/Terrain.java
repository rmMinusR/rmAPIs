package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.block.Block;

import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.AbstractCollider;
import rmMinusR.mc.plugins.apis.unitylike.physics.Line;
import rmMinusR.mc.plugins.apis.unitylike.physics.RaycastHit;

public class Terrain extends GameObject {
	
	public UUID worldID;
	
	public Terrain(World world) {
		super(world);
		Debug.Log("Initialized Raycasting collider for \""+world.getName()+"\"");
		worldID = world.getUID();
		AddComponent(new Collider(this));
	}
	
	public static class Collider extends AbstractCollider {
		
		public Collider(GameObject parent) {
			super(parent);
		}
		
		@Override
		public String toString() {
			return "Terrain.Collider("+gameObject.world.getName()+")";
		}
		
		@Override
		public boolean IsWithin(Vector3 point) {
			//Special case handling if x,y,z % 1 == 0
			float[] x = point.x%1==0 ? new float[] {point.x} : new float[] {point.x, point.x+1};
			float[] y = point.y%1==0 ? new float[] {point.y} : new float[] {point.y, point.y+1};
			float[] z = point.z%1==0 ? new float[] {point.z} : new float[] {point.z, point.z+1};
			
			for(float ix : x) for (float iy : y) for(float iz : z) {
				if(!IsWithin(new BlockVector3(ix, iy, iz))) return false;
			}
			
			return true;
		}
		
		public boolean IsWithin(BlockVector3 bv) {
			if(!gameObject.world.isChunkGenerated(bv.GetChunkX(), bv.GetChunkZ())) return false;
			return !gameObject.world.getBlockAt((int)bv.x, (int)bv.y, (int)bv.z).isEmpty();
		}
		
		private ArrayList<Block> CubeSurf(BlockVector3 center, int radius) {
			ArrayList<Block> out = new ArrayList<Block>();
			for(int x = -radius; x <= radius; x++) for(int y = -radius; y <= radius; y++) for(int z = -radius; z <= radius; z++) {
				
				if(x == -radius || x == radius
				|| y == -radius || y == radius
				|| z == -radius || z == radius) {
					BlockVector3 pos = new BlockVector3(x+center.x, y+center.y, z+center.z);
					Block b = pos.Fetch(gameObject.world);
					out.add(b);
				}
				
			}
			
			return out;
		}
		
		private ArrayList<Block> SelectRegion(BlockVector3 in1, BlockVector3 in2) {
			BlockVector3 lo = new BlockVector3(Math.min(in1.x, in2.x), Math.min(in1.y, in2.y), Math.min(in1.z, in2.z));
			BlockVector3 hi = new BlockVector3(Math.max(in1.x, in2.x), Math.max(in1.y, in2.y), Math.max(in1.z, in2.z));
			
			ArrayList<Block> region = new ArrayList<Block>();
			for(long x = lo.x; x <= hi.x; x++) for(long y = lo.y; y <= hi.y; y++) for(long z = lo.z; z <= hi.z; z++) {
				BlockVector3 i = new BlockVector3(x, y, z);
				Block b = i.Fetch(gameObject.world);
				if(b != null && !b.isEmpty()) region.add(b);
			}
			
			return region;
		}
		
		private ArrayList<Block> SelectByClosestApproach(ArrayList<Block> in, Line line) {
			ArrayList<Block> out = new ArrayList<Block>();
			
			for(Block b : in) {
				BlockVector3 lpos = new BlockVector3(b);
				Vector3 fpos = lpos.GetCenterOfBlock();
				
				float dist = fpos.Distance(line.GetClosestPoint(fpos));
				
				if(dist <= Math.sqrt(3)) out.add(b);
			}
			
			return out;
		}
		
		//FIXME does this even work???
		@Override
		public Vector3 GetClosestPoint(Vector3 global_point) {
			for(int r = 0; r < 1; r++) { //FIXME magic number
				ArrayList<Block> arr = new ArrayList<Block>();
				for(Block b : CubeSurf(global_point.ToBlockVector3(), r)) if(IsWithin(new BlockVector3(b))) arr.add(b);
				
				int nn = 0;
				for(Block b : arr) if(b != null && !b.isEmpty()) nn++;
				System.out.println("r="+r+" n="+arr.size()+" nn="+nn);
				
				BlockCollider closest = null; //FIXME ERROR IS SOMEWHERE IN THE NEXT BLOCK. Probably.
				for(Block b : arr) {
					if(b != null) {
						BlockCollider bc = new BlockCollider(b);
						if(closest == null) { closest = bc; continue; }
						//System.out.println(new BlockVector3(b).toString()+", "+b.getType().toString()+": "+bc.DistanceTo(global_point) + " < " + closest.DistanceTo(global_point));
						if(bc.DistanceTo(global_point) < closest.DistanceTo(global_point)) closest = bc;
					}
				}
				if(closest != null) return closest.GetClosestPoint(global_point);
			}
			return null;
		}
		
		//Technically it's a raymarch
		@Override
		public RaycastHit TryRaycast(Line _ray, float max_dist) {
			Line ray = new Line(_ray.origin, _ray.direction.Normalize());
			
			float step = 1;
			float t = 0;
			for(int iter = 0; iter < max_dist; iter++) {
				Vector3 cur_pos = ray.GetByT(t);
				
				ArrayList<Block> possibleCollisions = SelectByClosestApproach(
							SelectRegion(
									ray.GetByT(t).ToBlockVector3(),
									ray.GetByT(t+step).ToBlockVector3()
							),
							ray
						);
				
				RaycastHit collision = null;
				for(Block b : possibleCollisions) {
					BlockCollider b_coll = new BlockCollider(b);
					RaycastHit b_hit = b_coll.TryRaycast(ray, max_dist);
					if(b_hit != null) {
						if(collision == null || _ray.origin.Distance(collision.point) > _ray.origin.Distance(b_hit.point)) {
							collision = b_hit;
						}
					}
				}
				if(collision != null) return collision;
				
				if(cur_pos.y < 0 || cur_pos.y > gameObject.world.getMaxHeight()) return null;
				
				t += step;
			}
			
			return null;
		}

		@Override
		public void DebugRender() {
			
		}
	}
	
}
