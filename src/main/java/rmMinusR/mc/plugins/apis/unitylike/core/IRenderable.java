package rmMinusR.mc.plugins.apis.unitylike.core;

import rmMinusR.mc.plugins.apis.unitylike.Debug;

import java.util.Collection;
import java.util.HashSet;

public interface IRenderable {

	public default Collection<RenderDelegate> _PreRender() {
		try {
			return PreRender();
		} catch(Throwable t) { Debug.Log("Error calling PreRender() on "+this); t.printStackTrace(); return new HashSet<RenderDelegate>(); }
	}

	public Collection<RenderDelegate> PreRender();

	public Scene GetContext();

}
