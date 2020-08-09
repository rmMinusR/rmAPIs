package rmMinusR.mc.plugins.apis.unitylike;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public final class Debug {
	
	public static void Log(Object o) {
		RmApisPlugin.INSTANCE.logger.info("[rmAPIs] "+SafeToString(o));
	}
	
	public static String SafeToString(Object o) {
		return o != null ? o.toString() : "{null}";
	}
	
}
