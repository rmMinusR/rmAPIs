package rmMinusR.mc.plugins.apis.unitylike.data;

public final class Mathf {
	
	private Mathf() {}
	
	public static float Abs(float f) { return f > 0 ? f : -f; }

	public static float Acos(float f) { return (float) Math.acos(f); }

	public static boolean Approximately(float a, float b) { return Abs(a-b) < 0.0001f; } // FIXME magic number

	public static float Asin(float f) { return (float) Math.asin(f); }

	public static float Atan(float f) { return (float) Math.atan(f); }

	public static float Atan2(float y, float x) { return (float) Math.atan2(y, x); }

	public static float Ceil(float f) { return (float) Math.ceil(f); }
	
	public static int CeilToInt(float f) { return (int) Ceil(f); }
	
	public static float Clamp(float x, float lo, float hi) { return Max(lo, Min(x, hi)); }

	public static float Clamp01(float f) { return Clamp(f, 0, 1); }

	public static int ClosestPowerOfTwo(float f) {
		double exp = Math.log(f)/Math.log(2);
		return 2 << (int) Math.round(exp);
	}
	
	public static float Max(float a, float b) { return a > b ? a : b; }
	
	public static float Min(float a, float b) { return a < b ? a : b; }
	
	public static float Floor(float f) { return (float) Math.floor(f); }
	
	public static int FloorToInt(float f) { return (int) Floor(f); }
	
	public static float Lerp(float x, float a, float b) { return (1-x)*a + x*b; }
}
