package rmMinusR.mc.plugins.apis.unitylike;

import java.util.logging.Level;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public final class Debug {
	
	public static void Assert(boolean b, String errmsg) {
		if(!b) LogError(errmsg);
	}
	
	public static String Format(String s) { return String.format("[%s] [%s] %s", "rmAPIs", GetCaller(), s); }
	
	public static void Log(Object o) {
		RmApisPlugin.INSTANCE.logger.info(Format(MakeNullSafe(o)));
	}
	
	public static <T> T LogInPlace(T o) { Log(o); return o; }
	
	public static void LogWarning(Object o) {
		RmApisPlugin.INSTANCE.logger.warning(Format(MakeNullSafe(o)));
	}
	
	public static void LogError(Object o) {
		RmApisPlugin.INSTANCE.logger.log(Level.SEVERE, Format(MakeNullSafe(o)));
	}
	
	private static String GetCaller() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for(StackTraceElement t : trace) {
			if(!t.getClassName().equals(Debug.class.getName()) && !t.getClassName().equals(Thread.class.getName())) {
				String[] classpath = t.getClassName().split("\\.");
				return classpath[classpath.length-1]+"."+t.getMethodName();
			}
		}
		return "ERROR - UNTRACEABLE";
	}
	
	public static String MakeNullSafe(Object o) {
		return o != null ? o.toString() : "{null}";
	}
	
}
