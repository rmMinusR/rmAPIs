package rmMinusR.mc.plugins.apis.guichest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryExt {
	
	private InventoryExt() {}
	
	public static boolean Matches(Inventory a, Inventory b) {
		if(b.getSize() != a.getSize()) return false;
		
		for(int i = 0; i < b.getSize(); i++) {
			
			ItemStack ia = a.getStorageContents()[i];
			ItemStack ib = b.getStorageContents()[i];
			
			if( ia.getAmount() != ib.getAmount() || !ia.isSimilar(ib) ) return false;
		}
		return true;
	}
	
}
