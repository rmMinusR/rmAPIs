package rmMinusR.mc.plugins.apis.guichest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public class GUIChest implements Listener { //WARNING UNTESTED

	private Inventory surf;
	private ItemStackCallback[] callbacks;
	private Player viewer;
	
	public GUIChest(Player viewer, int rows, String name) {
		this.viewer = viewer;
		surf = Bukkit.createInventory(null, rows*9, name);
		callbacks = new ItemStackCallback[rows*9];
	}
	
	public boolean Matches(Inventory other) { return InventoryExt.Matches(surf, other); }
	
	public void Show() {
		viewer.openInventory(surf);
		
		//Register Listener
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
	}
	
	public void OnClose() {
		//Deregister Listener
		HandlerList.unregisterAll(this);
		
		//Close inventory, if we're still open
		Bukkit.getScheduler().runTask(RmApisPlugin.INSTANCE, new InvCloseHelper());
	}
	
	public class InvCloseHelper implements Runnable {
		
		@Override
		public void run() {
			if( viewer.getOpenInventory() != null && Matches(viewer.getOpenInventory().getTopInventory()) ) {
				viewer.closeInventory();
			}
		}
		
	}
	
	public void SetItem(int slot, ItemStackCallback item) {
		if(slot < 0 || slot >= callbacks.length) throw new IndexOutOfBoundsException();
		
		callbacks[slot] = item;
		surf.setItem(slot, item.renderItem);
	}
	
	@EventHandler
	public void OnInventoryClose(InventoryCloseEvent event) {
		if(viewer != event.getPlayer() || !Matches(event.getInventory())) return;
		
		OnClose();
	}
	
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent event) {
		if(viewer != event.getWhoClicked() || !Matches(event.getClickedInventory())) return;
		
		event.setCancelled(true);
		
		callbacks[event.getRawSlot()].OnClick(event.getClick());
	}

	public static class ItemStackCallback {
		
		public ItemStack renderItem;
		
		public ItemStackCallback(ItemStack renderItem) {
			this.renderItem = renderItem;
		}
		
		//Override me!
		public void OnClick(ClickType clickType) {}
		
	}
}
