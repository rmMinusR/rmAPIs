package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import org.bukkit.block.Block;

import rmMinusR.mc.plugins.apis.unitylike.core.GameObject;
import rmMinusR.mc.plugins.apis.unitylike.data.BlockVector3;
import rmMinusR.mc.plugins.apis.unitylike.data.MatrixTransform;
import rmMinusR.mc.plugins.apis.unitylike.data.Transform;
import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.BoxCollider;

public final class BlockCollider extends BoxCollider {
	public Block ref;
	
	@SuppressWarnings("deprecation")
	public BlockCollider(Block b) {
		super(new FakeGO(b), Vector3.zero(), Vector3.one(), BoxCollider.getLTW_NoRotation);
		ref = b;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+"{block="+ref+"}";
	}
	
	public static class FakeGO extends GameObject {
		
		private BlockVector3 pos;
		
		public FakeGO(Block ref) {
			super(ref.getWorld());
			this.pos = new BlockVector3(ref);
		}
		
		@Override
		public Transform GetTransform() {
			return new MatrixTransform(pos.GetCenterOfBlock());
		}
		
	}

}