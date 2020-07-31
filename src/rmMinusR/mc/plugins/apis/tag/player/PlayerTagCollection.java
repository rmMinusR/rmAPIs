package rmMinusR.mc.plugins.apis.tag.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import rmMinusR.mc.plugins.apis.tag.data.Value;

public class PlayerTagCollection {
	
	private PlayerTagCollection(Player host) {
		this.host = host;
		tags = new HashMap<String, Value>();
	}
	
	public static PlayerTagCollection generateNew(Player player) {
		return new PlayerTagCollection(player);
	}
	
	private PlayerTagCollection(File file) throws IOException {
		//What if the player is offline?
		host = Bukkit.getServer().getPlayer(UUID.fromString(file.getName()));
		
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
	
	public static PlayerTagCollection fromFile(File file) {
		try {
			if(!file.exists()) file.createNewFile();
			if(file.exists()) {
				return new PlayerTagCollection(file);
			} else {
				return generateNew(Bukkit.getPlayer(UUID.fromString(file.getName())));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public Player host;
	
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
}
