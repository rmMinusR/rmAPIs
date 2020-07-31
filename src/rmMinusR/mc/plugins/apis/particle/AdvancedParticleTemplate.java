package rmMinusR.mc.plugins.apis.particle;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class AdvancedParticleTemplate {
	
	Particle parent;
	Vector velocity;
	
	public AdvancedParticleTemplate(Particle parent) {
		this.parent = parent;
		velocity = new Vector(0, 0, 0);
	}
	
	public AdvancedParticleTemplate copy() {
		AdvancedParticleTemplate out = new AdvancedParticleTemplate(parent);
		out.velocity = velocity;
		return out;
	}
	
	public AdvancedParticleTemplate setVelocity(Vector vel) { velocity = vel; return this; }
	public AdvancedParticleTemplate setVelocity(double x, double y, double z) { velocity = new Vector(x, y, z); return this; }
	
	public AdvancedParticleTemplate setColor(double r, double g, double b) { velocity = ParticleGraphics.constructColor(r, g, b); return this; }
	
	public void instantiate(World w, Vector pos) {
		w.spawnParticle(parent, pos.toLocation(w), 0, velocity.getX(), velocity.getY(), velocity.getZ());
	}
	
	public void instantiateOne(World w, Vector pos, Player player) {
		//TODO write, requires PacketLib
	}
}
