package rmMinusR.mc.plugins.apis.noise;

public class PerlinNoise {
	
	public PerlinLayer[] layers;
	
	public PerlinNoise(int n_layers, int seed) {
		layers = new PerlinLayer[n_layers];
		for(int i = 0; i < n_layers; i++) {
			layers[i] = new PerlinLayer(seed+i, 1+i*(float)Math.sqrt(2));
		}
	}
	
	public float NoiseAt(float x, float y, float z) {
		float noise = 0;
		
		for(PerlinLayer l : layers) noise += l.NoiseAt(x, y, z);
		
		return noise/layers.length;
	}
	
}
