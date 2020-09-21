package rmMinusR.mc.plugins.apis.unitylike.core;

public final class Time {
	
	private Time() {} //Prevent instantiation
	
	private static long timeStart = 0;
	public static double time = 0;
	public static double deltaTime = 0;
	public static double timeSpentTicking = 0;

	private static double GetSecondsSinceStart() {
		return (System.nanoTime()-timeStart)/1000d/1000d/1000d;
	}
	
	public static void Init() {
		timeStart = System.currentTimeMillis();
		time = 0;
	}
	
	public static void OnFrameStart() {
		double pTime = time;
		time = GetSecondsSinceStart();
		
		deltaTime = time-pTime;
	}

	public static void OnFrameEnd() {
		timeSpentTicking = GetSecondsSinceStart() - time;
	}

	//For precise timed effects
	public static float MaxDeltaTime(float max) { return (float)(deltaTime > max ? max : deltaTime); }

	public static float Since(double when) {
		return (float)(time-when);
	}
	
	public static float Until(double when) {
		return (float)(when-time);
	}
	
}
