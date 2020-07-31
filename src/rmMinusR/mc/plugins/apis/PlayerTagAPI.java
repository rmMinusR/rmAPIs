package rmMinusR.mc.plugins.apis;

import java.util.HashMap;

import org.bukkit.entity.Player;

import rmMinusR.mc.plugins.apis.tag.data.Value;
import rmMinusR.mc.plugins.apis.tag.player.PlayerTagCollection;
public class PlayerTagAPI {
	
	public static void setTag(Player p, String key, Value value) {
		if(!hasTags(p)) throw new IllegalArgumentException("No tags on this player!");
		if(RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.containsKey(key)) RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.remove(key);
		ensureLoaded(p);
		RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.put(key, value);
	}
	
	public static Value popTag(Player p, String key, Value value) {
		if(!hasTags(p)) throw new IllegalArgumentException("No tags on this player!");
		ensureLoaded(p);
		return RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.remove(key);
	}
	
	public static Value getTag(Player p, String key) {
		if(!hasTags(p)) throw new IllegalArgumentException("No tags on this player!");
		if(!hasTag(p, key)) throw new IndexOutOfBoundsException("Key not found on this player!");
		ensureLoaded(p);
		return RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.get(key);
	}
	
	public static HashMap<String, Value> getAllTags(Player p) {
		if(!hasTags(p)) throw new IllegalArgumentException("No tags on this player!");
		ensureLoaded(p);
		return RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags;
	}
	
	public static boolean hasTags(Player p) {
		return RmApisPlugin.INSTANCE.playerTagManager.fetch(p) != null;
	}
	
	public static boolean hasTag(Player p, String key) {
		if(!hasTags(p)) throw new IllegalArgumentException("No tags on this player!");
		ensureLoaded(p);
		return RmApisPlugin.INSTANCE.playerTagManager.fetch(p).tags.containsKey(key);
	}

	public static void enableTags(Player target) {
		if(hasTags(target)) throw new IllegalArgumentException("Player is already tagged!");
		RmApisPlugin.INSTANCE.playerTagManager.loadedTags.add(PlayerTagCollection.generateNew(target));
	}
	
	public static void ensureLoaded(Player p) {
		RmApisPlugin.INSTANCE.playerTagManager.loadedTags.add(PlayerTagCollection.fromFile(RmApisPlugin.INSTANCE.playerTagManager.fileOf(p)));
	}
	
}
