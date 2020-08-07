package rmMinusR.mc.plugins.apis.noise;

import java.util.Random;

import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;

public class PerlinLayer {
	
	public int seed;
	public float scale;
	
	private Random rand;
	
	public PerlinLayer(int seed, float scale) {
		this.seed = seed;
		
		rand = new Random();
	}
	
	public float NoiseAt(float _x, float _y, float _z) {
		float x = _x/scale;
		float y = _y/scale;
		float z = _z/scale;
		
		float n000 = NoiseAtLattice(Mathf.FloorToInt(x), Mathf.FloorToInt(y), Mathf.FloorToInt(z));
		float n001 = NoiseAtLattice(Mathf.FloorToInt(x), Mathf.FloorToInt(y), Mathf. CeilToInt(z));
		float n010 = NoiseAtLattice(Mathf.FloorToInt(x), Mathf. CeilToInt(y), Mathf.FloorToInt(z));
		float n011 = NoiseAtLattice(Mathf.FloorToInt(x), Mathf. CeilToInt(y), Mathf. CeilToInt(z));
		float n100 = NoiseAtLattice(Mathf. CeilToInt(x), Mathf.FloorToInt(y), Mathf.FloorToInt(z));
		float n101 = NoiseAtLattice(Mathf. CeilToInt(x), Mathf.FloorToInt(y), Mathf. CeilToInt(z));
		float n110 = NoiseAtLattice(Mathf. CeilToInt(x), Mathf. CeilToInt(y), Mathf.FloorToInt(z));
		float n111 = NoiseAtLattice(Mathf. CeilToInt(x), Mathf. CeilToInt(y), Mathf. CeilToInt(z));
		
		float nx00 = Mathf.Lerp(x%1, n000, n100);
		float nx01 = Mathf.Lerp(x%1, n001, n101);
		float nx10 = Mathf.Lerp(x%1, n010, n110);
		float nx11 = Mathf.Lerp(x%1, n011, n111);
		
		float nxy0 = Mathf.Lerp(y%1, nx00, nx10);
		float nxy1 = Mathf.Lerp(y%1, nx01, nx11);
		
		return Mathf.Lerp(z%1, nxy0, nxy1);
	}
	
	private float NoiseAtLattice(int... coords) {
		long q = 0;
		for(int i = 0; i < coords.length; i++) q = q ^ (coords[i] << i - coords[i] >> i + (seed << i)^(seed >> i)); //FIXME more entropy!
		
		rand.setSeed(q);
		
		return rand.nextFloat();
	}
	
}
