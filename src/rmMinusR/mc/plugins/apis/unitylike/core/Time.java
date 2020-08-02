package rmMinusR.mc.plugins.apis.unitylike.core;

public final class Time {
	
	private Time() {} //Prevent instantiation
	
	private static float timeStart = 0;
	public static float time = 0;
	public static float deltaTime = 0;
	
	private static float GetSecondsSinceStart() {
		return System.currentTimeMillis()/1000f - timeStart;
	}
	
	public static void Init() {
		timeStart = System.currentTimeMillis()/1000f;
		time = 0;
	}
	
	public static void Update() {
		float pTime = time;
		time = GetSecondsSinceStart();
		
		deltaTime = time-pTime;
	}
	
}
