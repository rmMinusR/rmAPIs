package rmMinusR.mc.plugins.apis.unitylike.core;

public interface IEnableable {
	
	public boolean IsEnabled();
	
	public default void SetEnabled(boolean enable) {
		if(enable != IsEnabled()) {
			if(enable) {
				OnEnable();
			} else {
				OnDisable();
			}
		}
	}
	
	public void OnEnable();
	public void OnDisable();
	
}
