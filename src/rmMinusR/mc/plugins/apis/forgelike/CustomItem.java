package rmMinusR.mc.plugins.apis.forgelike;

import java.lang.reflect.Constructor;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;

public abstract class CustomItem {
	
	public static final String KEY_API_VERSION = "ForgelikeVersion";
	public static final int    VAL_API_VERSION = 1;
	public static final String KEY_TYPE        = "ForgelikeType";
	public static final String KEY_CUSTOM_DATA = "ForgelikeData";
	
	public ItemStack ref;
	public NBTCompound data;
	protected CustomItem(ItemStack ref, NBTCompound data) { this.ref = ref; this.data = data; }
	
	protected static final CustomItem Read(ItemStack stack) { return Read(stack, new NBTItem(stack)); }
	protected static final CustomItem Read(ItemStack stack, NBTItem nbt) throws IllegalStateException {
		if(!nbt.hasKey(KEY_TYPE)) throw new IllegalStateException("Missing item type!");
		
		//Assign defaults, if missing
		if(!nbt.hasKey(KEY_API_VERSION)) nbt.setInteger(KEY_API_VERSION, VAL_API_VERSION);
		if(!nbt.hasKey(KEY_CUSTOM_DATA)) nbt.addCompound(KEY_CUSTOM_DATA);
		
		//Write
		stack.setItemMeta(nbt.getItem().getItemMeta());
		
		//Read with defaults
		//int   r_apiversion = nbt.getInteger (KEY_API_VERSION);
		String        r_type = nbt.getString  (KEY_TYPE);
		NBTCompound   r_data = nbt.getCompound(KEY_CUSTOM_DATA);
		
		//TODO update format if necessary, using r_apiversion != VAL_API_VERSION
		
		try {
			//Attempt to find class
			@SuppressWarnings("unchecked")
			Class<? extends CustomItem> c_type = (Class<? extends CustomItem>) Class.forName(r_type);
			//Attempt to find ctor
			Constructor<? extends CustomItem> ctor = c_type.getConstructor(ItemStack.class, NBTCompound.class);
			//Attempt to instantiate
			return ctor.newInstance(stack, r_data);
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IllegalStateException("Tried to instantiate item \""+r_type+"\" but found no such class! Did you update/remove a plugin?");
		} catch (Throwable t) {
			throw new IllegalStateException("Tried to instantiate item \""+r_type+"\" but found no valid constructor!");
		}
	}
	
	public static final CustomItem CreateNewBlank(ItemStack target, Class<? extends CustomItem> clazz) {
		NBTItem nbt = new NBTItem(target);
		
		nbt.setString (KEY_TYPE, clazz.getName());
		
		//Write
		target.setItemMeta(nbt.getItem().getItemMeta());
		
		return Read(target, nbt);
	}
	
	public static final boolean IsCustomItem(ItemStack stack) {
		if(stack == null || stack.getType() == Material.AIR) return false;
		NBTItem item = new NBTItem(stack);
		return item.hasNBTData() && item.hasKey(KEY_API_VERSION);
	}
	
	public static final class Context {
		/*
		 * See https://wiki.vg/Inventory#Player_Inventory
		 */
		
		public final byte slot;
		public final Entity holder;
		
		private Context(Item groundItem) {
			this.slot = ON_GROUND_ID;
			this.holder = groundItem;
		}
		
		/*
		private Context(Chest chest) {
			this.slot = IN_CHEST_ID;
			this.groundItem = null;
			this.holder = chest.getBlockInventory();
		}
		*/
		
		private Context(byte data, Entity holder) {
			this.slot = data;
			this.holder = holder;
		}
		
		public static final byte ON_GROUND_ID = -3;
		//public static final byte  IN_CHEST_ID = -4;
		
		public boolean IsOnGround() { return slot == ON_GROUND_ID; }
		//public boolean IsInChest () { return slot ==  IN_CHEST_ID; }
		
		public boolean IsCursor() { return slot == -1; }
		
		public boolean IsCraft2x2Out() { return slot == 0; }
		public boolean IsCraft2x2In () { return 1 <= slot && slot <= 4; }
		
		public boolean IsArmor() { return IsArmorHead() || IsArmorBody() || IsArmorLegs() || IsArmorFeet(); }
		public boolean IsArmorHead() { return slot == ARMOR_HEAD_ID; }
		public boolean IsArmorBody() { return slot == ARMOR_BODY_ID; }
		public boolean IsArmorLegs() { return slot == ARMOR_LEGS_ID; }
		public boolean IsArmorFeet() { return slot == ARMOR_FEET_ID; }
		public static final byte ARMOR_HEAD_ID = 5;
		public static final byte ARMOR_BODY_ID = 6;
		public static final byte ARMOR_LEGS_ID = 7;
		public static final byte ARMOR_FEET_ID = 8;
		
		public boolean IsMainInventory() { return 9 <= slot && slot <= 35; }
		public boolean IsHotbar() { return 36 <= slot && slot <= 44; }
		
		public boolean IsOffHand() { return slot == 45; }
		
		public static Context Detect(ItemStack what, PlayerInventory inv) {
			for(byte i = 0; i < inv.getSize(); i++) if(what.equals(inv.getItem(i))) return BySlotID(i, inv.getHolder());
			return null;
		}
		public static Context BySlotID( byte  data, Entity holder) { return new Context(data, holder); }
		public static Context OnGround( Item  item) { return new Context( item); }
		//public static Context InChest (Chest chest) { return new Context(chest); }
		
	}
	
	public void OnTick(Context context, LivingEntity holder) { }
	
	public boolean OnLeftClick(LivingEntity holder) { return true; }
	
	public boolean OnRightClick(LivingEntity holder) { return true; }
	
	public abstract CustomMaterial GetMaterial();
	
	public void OnExitScope() {
		Write();
	}
	
	public void Write() {
		NBTItem nbt = new NBTItem(ref);
		
		nbt.setInteger(KEY_API_VERSION, VAL_API_VERSION);
		nbt.setString(KEY_TYPE, getClass().getName());
		
		nbt.removeKey(KEY_CUSTOM_DATA);
		NBTCompound tgt_data = nbt.addCompound(KEY_CUSTOM_DATA);
		tgt_data.mergeCompound(data);
		
		ref.setItemMeta(nbt.getItem().getItemMeta());
	}
	
	@Override
	public int hashCode() {
		return ref.hashCode()^getClass().hashCode();
	}
	
}
