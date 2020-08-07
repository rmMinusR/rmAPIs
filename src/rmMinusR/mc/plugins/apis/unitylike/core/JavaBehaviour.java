package rmMinusR.mc.plugins.apis.unitylike.core;

import org.bukkit.event.inventory.ClickType;

public abstract class JavaBehaviour extends Component {
	
	public JavaBehaviour(GameObject gameObject) {
		super(gameObject);
	}
	
	//SINGLE-CALL PATTERN: Awake()
	private boolean _calledAwake = false;
	protected final void _Awake() {
		if(!_calledAwake) {
			_calledAwake = true;
			Awake();
		}
	}
	
	public void Awake() {}
	//END
	
	//ENABLE/DISABLE PATTERN
	private boolean enabled = false;
	public final boolean IsEnabled() { return enabled; }
	
	public final void _SetEnabled(boolean enable) {
		if(enable != this.enabled) {
			this.enabled = enable;
			
			if(enable) {
				OnEnable();
			} else {
				OnDisable();
			}
		}
	}
	
	public void OnEnable() {}
	public void OnDisable() {}
	//END
	
	//SINGLE-CALL PATTERN: Start()
	private boolean _calledStart = false;
	protected final void _Start() {
		if(!_calledStart) {
			_calledStart = true;
			Start();
		}
	}
	
	public void Start() {}
	//END
	
	public void OnClick(ClickType type) {}
	
	public void Update() {}
	public void LateUpdate() {}
	
	//DIRTY RENDERING PATTERN
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
	
	@Override
	public void OnDestroy() {}
	
}
