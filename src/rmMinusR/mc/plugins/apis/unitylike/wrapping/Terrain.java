package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

import org.bukkit.World;
import org.bukkit.block.Block;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.Plane;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.AbstractCollider;
import rmMinusR.mc.plugins.apis.unitylike.physics.Line;
import rmMinusR.mc.plugins.apis.unitylike.physics.RaycastHit;

public class Terrain extends GameObject {
	
	public UUID worldID;
	
	public Terrain(World world) {
		super(world);
		System.out.println("New collider for world "+world.getName());
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
		
		private ArrayList<RaycastHit> NextBlockBoundaries(Vector3 fpos, Vector3 dir) {
			Line line = new Line(fpos, dir);
			BlockVector3 lpos = new BlockVector3(fpos);
			
			ArrayList<Plane> planes = new ArrayList<Plane>();
			
			for(Vector3 ord : Vector3.ordinals()) {
				planes.add( new Plane(
								lpos.GetCenterOfBlock().Add(
									ord.WithMagnitude(0.5f)
								),
								ord
						) );
			}
			
			ArrayList<RaycastHit> transitions = new ArrayList<RaycastHit>();
			for(Plane p : planes) {
				RaycastHit h = new RaycastHit();
				h.point = p.GetIntercept(line);
				h.normal = p.normal.clone(); //TODO why does this always point towards axial-negative
				
				BlockVector3 blockPos = h.point.Sub(h.normal.WithMagnitude(0.05f)).ToBlockVector3();
				Block block = blockPos.Fetch(gameObject.world);
				h.collider = new BlockCollider(block);
				
				if(line.GetTAt(h.point) > 0.01f) transitions.add(h);
			}
			
			transitions.sort(new Comparator<RaycastHit>() {
				@Override
				public int compare(RaycastHit a, RaycastHit b) {
					float da = Vector3.Distance(fpos, a.point);
					float db = Vector3.Distance(fpos, b.point);
					if(da > db) return 1;
					if(da < db) return -1;
					return 0;
				}
			});
			
			return transitions;
		}
		
		//FIXME not ideal...
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
		public RaycastHit TryRaycast(Line ray) {
			System.out.println("Terrain raymarching on "+ray.toString());
			
			float d = 0;
			for(int iter = 0; iter < 32; iter++) {
				Vector3 cur_pos = ray.GetByDistance(d);
				
				System.out.println("Iteration "+iter+", D="+d+" pos="+cur_pos);
				
				float step = 1;
				ArrayList<RaycastHit> nextBlockBoundaries = NextBlockBoundaries(cur_pos, ray.direction);
				
				for(RaycastHit boundary : nextBlockBoundaries) {
					Block nextBlock = ((BlockCollider)boundary.collider).ref;
					if(nextBlock != null && !nextBlock.isEmpty()) {
						System.out.println("Found block! Edge="+boundary.point);
						boundary.collider = new BlockCollider(nextBlock);
						return boundary;
					}
				}
				
				d += step;
			}
			
			return null;
		}
	}
	
}
