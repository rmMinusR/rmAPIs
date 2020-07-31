package rmMinusR.mc.plugins.apis.tag.item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.tag.data.Value;

public class ItemTagCollection {
	
	//IDS
	
	protected static long currentID;
	
	public static void detectID() {
		if(RmApisPlugin.INSTANCE.itemTagManager.workingDirectory.list().length > 0) {
			String[] names = RmApisPlugin.INSTANCE.itemTagManager.workingDirectory.list();
			Arrays.sort(names);
			currentID = Long.parseLong(names[names.length-1], 36);
		} else {
			currentID = 0;
		}
	}
	
	private static String generateID() {
		currentID++;
		return Long.toString(currentID, 36);
	}
	
	//BLANK CONSTRUCTORS
	
	private ItemTagCollection(String host) {
		this.host = host;
		tags = new HashMap<String, Value>();
	}
	
	public static ItemTagCollection generateAndBindNew(ItemStack item) {
		String id = generateID();
		setID(item, id);
		return new ItemTagCollection(id);
	}
	
	public static ItemTagCollection generateNew(String id) {
		return new ItemTagCollection(id);
	}
	
	//FILE CONSTRUCTORS
	
	private ItemTagCollection(File file) throws IOException {
		host = file.getName();
		
		//Read
		FileInputStream f = new FileInputStream(file);
		String str = "";
		while(f.available() > 0) {
			char c = (char) ((byte)f.read());
			str = str + c;
		}
		f.close();
		
		tags = new HashMap<String, Value>();
		for(String entry : str.split("\n")) {
			if(entry.replaceAll("\n", "").length() != 0 && entry.contains("\t")) {
				tags.put(entry.split("\t")[0], Value.fromString(entry.split("\t")[1]));
			}
		}
	}
	
	public static ItemTagCollection fromFile(File file) {
		try {
			return new ItemTagCollection(file);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String host;
	
	public HashMap<String, Value> tags;
	
	public String serialize() {
		String out = "";
		
		for(Entry<String, Value> e : tags.entrySet()) {
			out = out + e.getKey() + "\t" + e.getValue().serialize() + "\n";
		}
		
		return out;
	}
	
	public void save(File file) throws IOException {
		if(!file.exists()) file.createNewFile();
		
		FileOutputStream f = new FileOutputStream(file);
		f.write(serialize().getBytes());
		f.close();
	}
	
	//MISC.
	
	@Override
	public String toString() {
		return "[#"+host+"/"+tags.toString()+"]";
	}
	
	public static String prefix = "Custom item by rmAPIs - ID ";
	
	public static String getID(ItemStack item) {
		return item.getItemMeta().getLore().get(0).replaceFirst(prefix, "");
	}
	
	public static void setID(ItemStack item, String s) {
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(prefix+s);
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
}
