package rmMinusR.mc.plugins.apis.unitylike.core;

import rmMinusR.mc.plugins.apis.unitylike.Debug;

public abstract class RenderDelegate implements Comparable<RenderDelegate> {
	
	public final IRenderable owner;
	
	public RenderDelegate(IRenderable owner) {
		this.owner = owner;
	}

	public final int _GetPriority() {
		try {
			return GetPriority();
		} catch(Throwable t) { Debug.Log("Error calling GetPriority() on "+this); t.printStackTrace(); return 0; }
	}
	protected abstract int GetPriority();
	
	public final void _Render() {
		try {
			Render();
		} catch(Throwable t) { Debug.Log("Error calling Render() on "+this); t.printStackTrace(); }
	}
	protected abstract void Render();
	
	/*
	 * INCONSISTENT WITH EQUALS
	 */
	@Override
	public int compareTo(RenderDelegate o) {
		return o.GetPriority()-this.GetPriority();
	}
	
	@Override
	public String toString() {
		return owner+": "+getClass().getName();
	}
	
}
