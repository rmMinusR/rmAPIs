package rmMinusR.mc.plugins.apis.particle;

import org.bukkit.Color;

public class Image {
	public Color[][] data;
	
	public final int w, h;
	
	public Image(int w, int h) {
		this.w = w;
		this.h = h;
		
		data = new Color[w][];
		for(int ix = 0; ix < w; ix++) {
			data[ix] = new Color[h];
			for(int iy = 0; iy < h; iy++) {
				data[ix][iy] = Color.fromRGB(0, 0, 0);
			}
		}
	}
}
