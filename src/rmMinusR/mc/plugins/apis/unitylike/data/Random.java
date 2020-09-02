package rmMinusR.mc.plugins.apis.unitylike.data;

import static rmMinusR.mc.plugins.apis.unitylike.data.Mathf.*;

public final class Random {
	
	private Random() {}
	
	//Singleton RNG pattern
	private static java.util.Random _rng;
	private static java.util.Random GetRNG() { if(_rng == null) _rng = new java.util.Random(); return _rng; }
	
	public static void InitState(long seed) { GetRNG().setSeed(seed); }
	
	public static Quaternion Rotation() {
		float u = GetRNG().nextFloat();
		float v = GetRNG().nextFloat();
		float w = GetRNG().nextFloat();
		
		//https://stackoverflow.com/questions/31600717/how-to-generate-a-random-quaternion-quickly
		//http://planning.cs.uiuc.edu/node198.html
		return new Quaternion(
					Sqrt(1-u)*Sin(2*PI*v),
					Sqrt(1-u)*Cos(2*PI*v),
					Sqrt(1-u)*Sin(2*PI*w),
					Sqrt(1-u)*Cos(2*PI*w)
				);
	}
	
	public static Vector3 OnUnitSphere() { return Rotation().forward().Normalize(); } //FIXME issue with axial 8-streaking? https://karthikkaranth.me/blog/generating-random-points-in-a-sphere/
	public static Vector3 InsideUnitSphere() { return Rotation().forward().WithMagnitude( Cbrt(GetRNG().nextFloat()) ); }
	
	public static double Range(double min, double max) { if(min > max) throw new IllegalArgumentException(); return GetRNG().nextDouble() * (max-min) + min; }
	public static float  Range(float  min, float  max) { if(min > max) throw new IllegalArgumentException(); return GetRNG().nextFloat() * (max-min) + min; }
	public static int    Range(int    min, int    max) { if(min > max) throw new IllegalArgumentException(); return min + GetRNG().nextInt() % (max-min+1); }
	public static long   Range(long   min, long   max) { if(min > max) throw new IllegalArgumentException(); return min + GetRNG().nextLong() % (max-min+1); }
	
}
