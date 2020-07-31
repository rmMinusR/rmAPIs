package rmMinusR.mc.plugins.apis.particle;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class ParticleGraphics {
	public static void drawLine(World w, Vector a, Vector b, AdvancedParticleTemplate p, double relstep) {
		double delta_i = relstep/a.distance(b);
		
		for(double i = 0.0; i < 1.0; i += delta_i ) {
			p.instantiate(w, new Vector(
							a.getX()*i + b.getX()*(1-i),
							a.getY()*i + b.getY()*(1-i),
							a.getZ()*i + b.getZ()*(1-i)
					));
		}
	}
	
	public static void wireQuad(World w, Vector a, Vector b, Vector c, Vector d, AdvancedParticleTemplate p, double relstep) {
		drawLine(w, a, b, p, relstep);
		drawLine(w, b, c, p, relstep);
		drawLine(w, c, d, p, relstep);
		drawLine(w, d, a, p, relstep);
	}
	
	public static void fillQuad(World w, Vector a, Vector b, Vector c, Vector d, AdvancedParticleTemplate p, double relstep) {
		double delta_i = relstep/a.distance(b);
		
		for(double i = 0.0; i < 1.0; i += delta_i) {
			Vector ab_side = a.clone().multiply(i).add(b.clone().multiply(1-i));
			Vector cd_side = d.clone().multiply(i).add(c.clone().multiply(1-i));
			
			drawLine(w, ab_side, cd_side, p, relstep);
		}
	}
	
	public static Vector min(Vector i, Vector j) {
		return new Vector(Math.min(i.getX(), j.getX()), Math.min(i.getY(), j.getY()), Math.min(i.getZ(), j.getZ()));
	}
	
	public static Vector max(Vector i, Vector j) {
		return new Vector(Math.max(i.getX(), j.getX()), Math.max(i.getY(), j.getY()), Math.max(i.getZ(), j.getZ()));
	}
	
	public static void wireCube(World w, Vector a, Vector b, AdvancedParticleTemplate p, double relstep) {
		wireCube(w, a, b, p, p, p, relstep);
	}
	
	public static void wireCube(World w, Vector a, Vector b, AdvancedParticleTemplate x, AdvancedParticleTemplate y, AdvancedParticleTemplate z, double relstep) {
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), a.getY(), a.getZ()),
				new Vector(b.getX(), a.getY(), a.getZ()),
				x, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), b.getY(), a.getZ()),
				new Vector(b.getX(), b.getY(), a.getZ()),
				x, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), b.getY(), b.getZ()),
				new Vector(b.getX(), b.getY(), b.getZ()),
				x, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), a.getY(), b.getZ()),
				new Vector(b.getX(), a.getY(), b.getZ()),
				x, relstep);
		
		
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), a.getY(), a.getZ()),
				new Vector(a.getX(), b.getY(), a.getZ()),
				y, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(b.getX(), a.getY(), a.getZ()),
				new Vector(b.getX(), b.getY(), a.getZ()),
				y, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(b.getX(), a.getY(), b.getZ()),
				new Vector(b.getX(), b.getY(), b.getZ()),
				y, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), a.getY(), b.getZ()),
				new Vector(a.getX(), b.getY(), b.getZ()),
				y, relstep);
		
		
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), a.getY(), a.getZ()),
				new Vector(a.getX(), a.getY(), b.getZ()),
				z, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(b.getX(), a.getY(), a.getZ()),
				new Vector(b.getX(), a.getY(), b.getZ()),
				z, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(b.getX(), b.getY(), a.getZ()),
				new Vector(b.getX(), b.getY(), b.getZ()),
				z, relstep);
		ParticleGraphics.drawLine(w,
				new Vector(a.getX(), b.getY(), a.getZ()),
				new Vector(a.getX(), b.getY(), b.getZ()),
				z, relstep);
	}
	
	public static void surfCube(World w, Vector a, Vector b, AdvancedParticleTemplate p, double relstep) {
		Vector aaa = a.clone();
		Vector baa = a.clone(); baa.setX(b.getX());
		Vector aba = a.clone(); aba.setY(b.getY());
		Vector aab = a.clone(); aab.setZ(b.getZ());

		Vector bbb = b.clone();
		Vector abb = b.clone(); abb.setX(a.getX());
		Vector bab = b.clone(); bab.setY(a.getY());
		Vector bba = b.clone(); bba.setZ(a.getZ());
		
		fillQuad(w, aaa, aba, abb, aab, p, relstep); //X-A
		fillQuad(w, bbb, bab, baa, bba, p, relstep); //X-B
		
		fillQuad(w, aaa, baa, bab, aab, p, relstep); //Y-A
		fillQuad(w, bbb, abb, aba, bba, p, relstep); //Y-B
		
		fillQuad(w, aaa, baa, bba, aba, p, relstep); //Z-A
		fillQuad(w, bbb, abb, aab, bab, p, relstep); //Z-B
	}
	
	public static Vector constructColor(double r, double g, double b) {
		return new Vector(r*0.999+0.001, g*0.999+0.001, b*0.999+0.001);
	}
	
	public static void drawImage(Image image, World w, Vector origin, Vector right, Vector up) {
		AdvancedParticleTemplate template = new AdvancedParticleTemplate(Particle.REDSTONE);
		for(int ix = 0; ix < image.w; ix++) for(int iy = 0; iy < image.h; iy++) {
			org.bukkit.Color px = image.data[ix][iy];
			Vector pos = origin.add(right.multiply(ix)).add(up.multiply(-iy));
			template.copy().setColor(px.getRed(), px.getGreen(), px.getBlue()).instantiate(w, pos);
		}
	}
}