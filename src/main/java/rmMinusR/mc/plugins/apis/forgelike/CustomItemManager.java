package rmMinusR.mc.plugins.apis.forgelike;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.comphenix.packetwrapper.WrapperPlayServerEntityEquipment;
import com.comphenix.packetwrapper.WrapperPlayServerSetSlot;
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem.Context;
import rmMinusR.mc.plugins.apis.guichest.InventoryExt;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.core.JavaBehaviour;
import rmMinusR.mc.plugins.apis.unitylike.core.Time;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedEntity;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedLivingEntity;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedPlayer;

/**
 * Handles logic for custom items wielded by Players or LivingEntities
 * One per player/entity, attached to a Wrap()'d entity
 */
public class CustomItemManager extends JavaBehaviour implements Listener {
	
	//Cache logic
	//TODO change cache system to per-entity
	private static final int maxCacheSize = 200;
	public static ArrayList<CustomItem> cache;
	public ItemRenderPacketInterceptor packetInterceptor;
	
	public CustomItemManager(WrappedLivingEntity gameObject) {
		super(gameObject);
		if(cache == null) cache = new ArrayList<CustomItem>();
		if(gameObject instanceof WrappedPlayer) packetInterceptor = new ItemRenderPacketInterceptor(((WrappedPlayer)gameObject).player);
	}
	
	protected static CustomItem GetOrInstantiate(ItemStack i) {
		if(cache == null) cache = new ArrayList<CustomItem>();
		if(!CustomItem.IsCustomItem(i)) return null;
		if(HasCached(i)) return RetrieveCached(i);
		
		return PutCached(CustomItem.Read(i));
	}
	
	private static CustomItem PutCached(CustomItem o) {
		if(o == null) return null;
		
		cache.add(o);
		
		while(cache.size() > maxCacheSize) Uncache(cache.get(0));
		
		return o;
	}
	
	private static CustomItem RetrieveCached(ItemStack o) {
		if(o == null) return null;
		
		for(CustomItem i : cache) if(o.getAmount() == i.ref.getAmount() && i.ref.isSimilar(o)) return i;
		
		return null;
	}
	
	private static void Uncache(CustomItem c) {
		if(c == null || !cache.contains(c)) return;
		
		cache.remove(c);
		c.OnExitScope();
	}
	
	private static boolean HasCached(ItemStack o) {
		if(o == null) return false;
		
		for(CustomItem i : cache) if(o.getAmount() == i.ref.getAmount() && i.ref.isSimilar(o)) return true;
		
		return false;
	}
	
	//Register/deregister for events handling
	
	@Override
	public void Awake() {
		RescanEntity((WrappedLivingEntity)gameObject);
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
		ProtocolLibrary.getProtocolManager().addPacketListener(packetInterceptor);
	}
	
	@Override
	public void OnDestroy() {
		ProtocolLibrary.getProtocolManager().removePacketListener(packetInterceptor);
		HandlerList.unregisterAll(this);
	}
	
	//Rescan logic
	
	@EventHandler
	public void OnInventoryAlteredByPlayer(InventoryEvent event) {
		if( ((WrappedLivingEntity)gameObject).entity instanceof InventoryHolder) if(InventoryExt.Matches( event.getInventory(), ((InventoryHolder)((WrappedLivingEntity)gameObject).entity).getInventory() )) RescanEntity((WrappedLivingEntity)gameObject);
	}
	
	@EventHandler
	public void OnItemPickup(EntityPickupItemEvent event) {
		if(!event.getEntity().getUniqueId().equals( ((WrappedEntity)gameObject).entity.getUniqueId() )) return;
		
		RescanEntity(event.getEntity());
	}
	
	@EventHandler
	public void OnItemDrop(PlayerDropItemEvent event) {
		RescanEntity(event.getPlayer());
		
		GetOrInstantiate(event.getItemDrop().getItemStack());
	}
	
	private static void RescanEntity(WrappedLivingEntity entity) { RescanEntity(entity.livingEntity); }
	private static void RescanEntity(LivingEntity entity) {
		for(ItemStack i : GetAllItems(entity)) GetOrInstantiate(i);
		
		if(entity instanceof InventoryHolder) {
			for(ItemStack i : ((InventoryHolder)entity).getInventory()) GetOrInstantiate(i);
		}
	}
	
	private static HashSet<ItemStack> GetAllItems(LivingEntity entity) {
		HashSet<ItemStack> out = new HashSet<ItemStack>();
		
		out.add(entity.getEquipment().getHelmet());
		out.add(entity.getEquipment().getChestplate());
		out.add(entity.getEquipment().getLeggings());
		out.add(entity.getEquipment().getBoots());
		
		out.add(entity.getEquipment().getItemInMainHand());
		out.add(entity.getEquipment().getItemInOffHand());
		
		if(entity instanceof InventoryHolder) for(ItemStack i : ((InventoryHolder)entity).getInventory()) out.add(i);
		
		out.remove(null);
		
		return out;
	}
	
	//Ticker
	
	@Override
	public void Update() {
		LivingEntity ent = ((WrappedLivingEntity)gameObject).livingEntity;
		if(ent instanceof Player) {
			Player p = (Player) ent;
			for(ItemStack i : p.getInventory()) if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.Detect(i, p.getInventory()), p);
		} else {
			{ ItemStack i = ent.getEquipment().getHelmet();     if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_HEAD_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getChestplate(); if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_BODY_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getLeggings();   if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_LEGS_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getBoots();      if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_FEET_ID, ent), ent); }

			//FIXME create and use Context.MAIN_HAND instead
			{ ItemStack i = ent.getEquipment().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_FEET_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getItemInOffHand();  if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.ByPacketSlotID(Context.ARMOR_FEET_ID, ent), ent); }
		}
	}
	
	//* click block
	
	private float tLastClickLeft = -100;
	private float tLastClickRight = -100;
	
	@EventHandler
	public void OnPlayerInteractBlock(PlayerInteractEvent event) {
		LivingEntity ent = ((WrappedLivingEntity)gameObject).livingEntity;
		if(event.getPlayer() != ent) return;
		
		boolean allowVanillaAction = true;
		
		if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(ent instanceof Player) {
				//DEBOUNCE PATTERN
				if(Time.Since(tLastClickLeft) < 0.02f) return;
				else tLastClickLeft = (float) Time.time;
				
				Player p = (Player) ent;
				{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
				{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
			}
		} else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(ent instanceof Player) {
				//DEBOUNCE PATTERN
				if(Time.Since(tLastClickRight) < 0.02f) return;
				else tLastClickRight = (float) Time.time;
				
				Player p = (Player) ent;
				{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
				{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
			}
		}
		
		if(!allowVanillaAction) event.setCancelled(true);
	}
	
	//Left click entity
	
	@EventHandler
	public void OnEntityAttackEntity(EntityDamageByEntityEvent event) {
		LivingEntity ent = ((WrappedLivingEntity)gameObject).livingEntity;
		if(event.getDamager() != ent) return;
		
		//DEBOUNCE PATTERN
		if(Mathf.Approximately(tLastClickLeft, Time.time)) return;
		else tLastClickLeft = (float) Time.time;
		
		boolean allowVanillaAction = true;
		
		if(ent instanceof Player) {
			Player p = (Player) ent;
			{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
			{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
		}
		
		if(!allowVanillaAction) event.setCancelled(true);
	}
	
	//Right click entity
	
	@EventHandler
	public void OnPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
		LivingEntity ent = ((WrappedLivingEntity)gameObject).livingEntity;
		if(event.getPlayer() != ent) return;
		
		//DEBOUNCE PATTERN
		if(Time.Since(tLastClickRight) < 0.02f) return;
		else tLastClickRight = (float) Time.time;
		
		boolean allowVanillaAction = true;
		
		if(ent instanceof Player) {
			Player p = (Player) ent;
			{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
			{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
		}
		
		if(!allowVanillaAction) event.setCancelled(true);
	}
	
	/*
	 * In GMC, Notchian client destroys and recreates all items for some stupid reason
	 * Therefore, to preserve item data, don't show item illusions when in GMC
	 * Must refresh illusion'd items using SetSlot packet when changing GM
	 */
	@EventHandler
	public void OnPlayerChangeGamemode(PlayerGameModeChangeEvent event) {
		if(!(gameObject instanceof WrappedPlayer)) return;
		Player p = ((WrappedPlayer)gameObject).player;
		if(! event.getPlayer().getUniqueId().equals( p.getUniqueId() )) return;
		
		//Refresh clientside inventory
		//FIXME use packets
		final Player tgt = event.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(RmApisPlugin.INSTANCE, new Runnable() {
			@Override
			public void run() {
				tgt.getPlayer().getInventory().setStorageContents(tgt.getPlayer().getInventory().getStorageContents());
			}
		});
		
		/*
		//Build list
		ItemStack[] itemsToRefresh = new ItemStack[event.getPlayer().getInventory().getSize()];
		for(int i = 0; i < itemsToRefresh.length; i++) {
			ItemStack item = event.getPlayer().getInventory().getItem(i);
			itemsToRefresh[i] = item!=null ? item : new ItemStack(Material.AIR);
		}
		
		//Known issues: Does not map correctly when in GMC
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(RmApisPlugin.INSTANCE, new Runnable() {
			@Override
			public void run() {
				WrapperPlayServerWindowItems out_wrapper = new WrapperPlayServerWindowItems();
				
				out_wrapper.setSlotData(Arrays.asList(itemsToRefresh));
				out_wrapper.sendPacket(event.getPlayer());
			}
		});
		*/
	}
	
	protected final class ItemRenderPacketInterceptor extends PacketAdapter {
		
		private Player receivingPlayer;
		public ItemRenderPacketInterceptor(Player reciever) {
			super(RmApisPlugin.INSTANCE, ListenerPriority.NORMAL,
					PacketType.Play.Server.WINDOW_ITEMS,
					PacketType.Play.Server.SET_SLOT,
					PacketType.Play.Server.ENTITY_EQUIPMENT,
					PacketType.Play.Server.GAME_STATE_CHANGE
				);
			this.receivingPlayer = reciever;
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			//Filter to just receiving player
			if(! event.getPlayer().getUniqueId().equals(receivingPlayer.getUniqueId())) return;
			
			//In GMC, Notchian client destroys and recreates all items for some stupid reason
			//Therefore, to preserve item data, don't show item illusions when in GMC
			if(receivingPlayer.getGameMode() == GameMode.CREATIVE) return;
			
			if(event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
				//
				WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event.getPacket());
				
				boolean hasChanged = false;
				List<ItemStack> items = wrapper.getSlotData();
				for(int i = 0; i < items.size(); i++) { //Can't be iterated in place due to concurrent modification
					ItemStack is = items.get(i);
					if(CustomItem.IsCustomItem(is)) {
						CustomItem ci = CustomItem.Read(is);
						ItemStack rendered_raw = ci.GetRenderType();
						
						if(rendered_raw != null) {
							ItemStack rendered_final = rendered_raw.clone();
							rendered_final.setAmount(is.getAmount());
							
							items.set(i, rendered_final);
							hasChanged = true;
						}
					}
				}
				if(hasChanged) wrapper.setSlotData(items);
				
			} else if(event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
				
				WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(event.getPacket());
				
				boolean hasChanged = false;
				ItemStack item = wrapper.getSlotData();
				if(CustomItem.IsCustomItem(item)) {
					CustomItem ci = CustomItem.Read(item);
					ItemStack rendered_raw = ci.GetRenderType();
					
					if(rendered_raw != null) {
						ItemStack rendered_final = rendered_raw.clone();
						rendered_final.setAmount(item.getAmount());
						
						item = rendered_final;
						hasChanged = true;
					}
				}
				if(hasChanged) wrapper.setSlotData(item);
				
			} else if(event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
				
				WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event.getPacket());
				
				boolean hasChanged = false;
				ItemStack item = wrapper.getItem();
				if(CustomItem.IsCustomItem(item)) {
					CustomItem ci = CustomItem.Read(item);
					ItemStack rendered_raw = ci.GetRenderType();
					
					if(rendered_raw != null) {
						ItemStack rendered_final = rendered_raw.clone();
						rendered_final.setAmount(item.getAmount());
						
						item = rendered_final;
						hasChanged = true;
					}
				}
				if(hasChanged) wrapper.setItem(item);
				
			}
			
		}
		
	}
	
}
