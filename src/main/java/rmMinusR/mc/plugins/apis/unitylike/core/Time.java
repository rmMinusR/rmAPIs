package rmMinusR.mc.plugins.apis.unitylike.core;

public final class Time {
	
	private Time() {} //Prevent instantiation
	
	private static long timeStart = 0;
	public static double time = 0;
	public static double deltaTime = 0;
	
	private static float GetSecondsSinceStart() {
		return (System.currentTimeMillis()-timeStart)/1000f;
	}
	
	public static void Init() {
		timeStart = System.currentTimeMillis();
		time = 0;
	}
	
	public static void Update() {
		double pTime = time;
		time = GetSecondsSinceStart();
		
		deltaTime = time-pTime;
	}
	
	public static float MaxDeltaTime(float max) { return (float)(deltaTime > max ? max : deltaTime); }

	public static float Since(double when) {
		return (float)(time-when);
	}
	
	public static float Until(double when) {
		return (float)(when-time);
	}
	
}
