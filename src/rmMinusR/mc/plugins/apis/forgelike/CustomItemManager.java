package rmMinusR.mc.plugins.apis.forgelike;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.forgelike.CustomItem.Context;
import rmMinusR.mc.plugins.apis.guichest.InventoryExt;
import rmMinusR.mc.plugins.apis.unitylike.core.JavaBehaviour;
import rmMinusR.mc.plugins.apis.unitylike.core.Time;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedEntity;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedLivingEntity;

/**
 * Handles logic for custom items wielded by Players or LivingEntities
 */
public class CustomItemManager extends JavaBehaviour implements Listener {
	
	//Cache logic
	private static final int maxCacheSize = 200;
	public static ArrayList<CustomItem> cache;
	
	public CustomItemManager(WrappedLivingEntity gameObject) {
		super(gameObject);
		if(cache == null) cache = new ArrayList<CustomItem>();
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
	}
	
	@Override
	public void OnDestroy() {
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
			{ ItemStack i = ent.getEquipment().getHelmet();     if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_HEAD_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getChestplate(); if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_BODY_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getLeggings();   if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_LEGS_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getBoots();      if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_FEET_ID, ent), ent); }
			
			{ ItemStack i = ent.getEquipment().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_FEET_ID, ent), ent); }
			{ ItemStack i = ent.getEquipment().getItemInOffHand();  if(CustomItem.IsCustomItem(i)) GetOrInstantiate(i).OnTick(Context.BySlotID(Context.ARMOR_FEET_ID, ent), ent); }
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
				else tLastClickLeft = Time.time;
				
				Player p = (Player) ent;
				{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
				{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnLeftClick(ent); }
			}
		} else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(ent instanceof Player) {
				//DEBOUNCE PATTERN
				if(Time.Since(tLastClickRight) < 0.02f) return;
				else tLastClickRight = Time.time;
				
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
		else tLastClickLeft = Time.time;
		
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
		else tLastClickRight = Time.time;
		
		boolean allowVanillaAction = true;
		
		if(ent instanceof Player) {
			Player p = (Player) ent;
			{ ItemStack i = p.getInventory().getItemInMainHand(); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
			{ ItemStack i = p.getInventory().getItemInOffHand (); if(CustomItem.IsCustomItem(i)) allowVanillaAction &= GetOrInstantiate(i).OnRightClick(ent); }
		}
		
		if(!allowVanillaAction) event.setCancelled(true);
	}
	
}
