package rmMinusR.mc.plugins.apis.unitylike.core;

public abstract class JavaBehaviour extends Component implements IEnableable {
	
	public JavaBehaviour(GameObject gameObject) {
		super(gameObject);
	}
	
	//ENABLE/DISABLE PATTERN
	private boolean _enabled = false;
	@Override
	public final boolean IsEnabled() { return _enabled; }
	
	@Override
	public final void SetEnabled(boolean enable) {
		IEnableable.super.SetEnabled(enable);
		_enabled = enable;
	}
	
	@Override
	public void OnEnable() {}
	
	@Override
	public void OnDisable() {}
	//END
	
	//DIRTY RENDERING PATTERN - TODO make iface
	private boolean _dirty = true;
	private boolean _IsDirty() { return _dirty; }
	
	public final void _Render() { _Render(false); }
	public final void _Render(boolean force) {
		if(force || _IsDirty()) {
			_dirty = false;
			Render();
		}
	}
	public int GetRenderOrder() { return -1; }
	public void Render() {}
	
	public void OnDestroy() {}
	
}
