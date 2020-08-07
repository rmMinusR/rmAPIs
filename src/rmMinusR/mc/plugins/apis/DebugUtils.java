package rmMinusR.mc.plugins.apis;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTType;

public class DebugUtils {

	public static String ToString(NBTCompound nbt) { return ToString(nbt, 0); }
	
	public static String ToString(NBTCompound nbt, int indent) {
		String out = "{";
		
		out += "\n";
		
		for(String key : nbt.getKeys()) {
			
			for(int i = 0; i < indent; i++) out += "   ";
			out += " - " + key + ": ";
			
			if(nbt.getType(key) == NBTType.NBTTagCompound) {
				out += ToString(nbt.getCompound(key), indent+1) + "\n";
			} else {
				out += nbt.getString(key) + "\n";
			}
			
		}
		
		for(int i = 0; i < indent; i++) out += "   ";
		out += "}";
		
		return out;
	}
	
	public static <T> T LogInPlace(T in) {
		RmApisPlugin.INSTANCE.logger.info(in.toString());
		return in;
	}
	
}
