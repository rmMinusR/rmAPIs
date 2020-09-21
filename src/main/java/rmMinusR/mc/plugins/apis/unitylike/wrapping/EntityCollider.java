package rmMinusR.mc.plugins.apis.unitylike.wrapping;

import rmMinusR.mc.plugins.apis.unitylike.data.Vector3;
import rmMinusR.mc.plugins.apis.unitylike.physics.BoxCollider;

public class EntityCollider extends BoxCollider {
	
	public EntityCollider(WrappedEntity wrappedEntity) {
		super(
				wrappedEntity,
				new Vector3(
						0,
						wrappedEntity.entity.getHeight()/2, //Minecraft entities' anchor is at their feet
						0
					),
				new Vector3(
						wrappedEntity.entity.getWidth(),
						wrappedEntity.entity.getHeight(),
						wrappedEntity.entity.getWidth()
					),
				BoxCollider.getLTW_YawOnly
			);
	}

}
