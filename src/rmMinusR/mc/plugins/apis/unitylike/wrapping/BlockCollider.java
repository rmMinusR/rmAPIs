package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.block.Block;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.BoxCollider;

public class BlockCollider extends BoxCollider {
	public Block ref;
	
	public BlockCollider(Block b) {
		super(null, Vector3.one(), Vector3.zero());
		ref = b;
		gameObject = new FakeGO(this);
	}
	
	public static class FakeGO extends GameObject {
		
		private BlockVector3 pos;
		
		public FakeGO(BlockCollider src) {
			super(src.ref.getWorld());
			this.pos = new BlockVector3(src.ref);
		}
		
		@Override
		public Transform GetTransform() {
			return new Transform(pos.GetCenterOfBlock());
		}
		
	}

}