package rmMinusR.mc.plugins.apis.unitylike.core;

public final class Time {
	
	private Time() {} //Prevent instantiation
	
	private static long timeStart = 0;
	public static float time = 0;
	public static float deltaTime = 0;
	
	private static float GetSecondsSinceStart() {
		return (System.currentTimeMillis()-timeStart)/1000f;
	}
	
	public static void Init() {
		timeStart = System.currentTimeMillis();
		time = 0;
	}
	
	public static void Update() {
		float pTime = time;
		time = GetSecondsSinceStart();
		
		deltaTime = time-pTime;
	}
	
	public static float MaxDeltaTime(float max) { return deltaTime > max ? max : deltaTime; }

	public static float Since(float when) {
		return time-when;
	}
	
	public static float Until(float when) {
		return when-time;
	}
	
}
