package rmMinusR.mc.plugins.apis;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import rmMinusR.mc.plugins.apis.tag.data.Value;
import rmMinusR.mc.plugins.apis.tag.item.ItemTagCollection;

public class ItemTagAPI {

	public static void setTag(ItemStack i, String key, Value value) {
		if(!hasTags(i)) throw new IllegalArgumentException("No tags on this item!");
		if(RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.containsKey(key)) RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.remove(key);
		ensureLoaded(i);
		RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.put(key, value);
	}
	
	public static Value popTag(ItemStack i, String key) {
		if(!hasTags(i)) throw new IllegalArgumentException("No tags on this item!");
		ensureLoaded(i);
		return RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.remove(key);
	}
	
	public static Value getTag(ItemStack i, String key) {
		if(!hasTags(i)) throw new IllegalArgumentException("No tags on this item!");
		if(!hasTag(i, key)) throw new IndexOutOfBoundsException("Key not found on this item!");
		ensureLoaded(i);
		return RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.get(key);
	}
	
	public static HashMap<String, Value> getAllTags(ItemStack i) {
		if(!hasTags(i)) throw new IllegalArgumentException("No tags on this item!");
		ensureLoaded(i);
		return RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags;
	}

	public static boolean hasTags(ItemStack i) {
		return RmApisPlugin.INSTANCE.itemTagManager.isValid(i);
	}
	
	public static boolean hasTag(ItemStack i, String key) {
		if(!hasTags(i)) throw new IllegalArgumentException("No tags on this item!");
		ensureLoaded(i);
		return RmApisPlugin.INSTANCE.itemTagManager.fetch(i).tags.containsKey(key);
	}

	public static void enableTags(ItemStack target) {
		if(hasTags(target)) throw new IllegalArgumentException("Item is already tagged!");
		RmApisPlugin.INSTANCE.itemTagManager.loadedTags.add(ItemTagCollection.generateAndBindNew(target));
	}

	public static void disableTags(ItemStack target) {
		if(!hasTags(target)) throw new IllegalArgumentException("No tags on this item!");
		ensureLoaded(target);
		RmApisPlugin.INSTANCE.itemTagManager.delete(target);
	}
	
	public static void ensureLoaded(ItemStack i) {
		RmApisPlugin.INSTANCE.itemTagManager.loadedTags.add(ItemTagCollection.fromFile(RmApisPlugin.INSTANCE.itemTagManager.fileOf(i)));
	}
	
}
