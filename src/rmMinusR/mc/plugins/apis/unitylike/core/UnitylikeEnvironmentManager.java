package rmMinusR.mc.plugins.apis.unitylike.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public final class UnitylikeEnvironmentManager implements Runnable, Listener {
	
	private ArrayList<GameObject> gameObjects;
	public float entityAddRadius;
	
	public UnitylikeEnvironmentManager(float entityAddRadius) {
		gameObjects = new ArrayList<GameObject>();
		this.entityAddRadius = entityAddRadius;
	}
	
	private int taskID;
	public void OnEnable() {
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RmApisPlugin.INSTANCE, this, 1, 1);
		Time.Init();
	}
	
	public void OnDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTask(taskID);
		
		ArrayList<GameObject> tmp = new ArrayList<GameObject>(); tmp.addAll(gameObjects);
		for(GameObject go : tmp) Destroy(go);
	}
	
	public WrappedEntity Wrap(Entity who) {
		//Try to fetch existing wrapper
		for(GameObject go : gameObjects) if(go instanceof WrappedEntity && ((WrappedEntity)go).entity == who) return (WrappedEntity)go;
		
		//Assume not wrapped yet, construct new wrapper
		WrappedEntity out = null;
		
		     if(who instanceof Player)       out = new WrappedPlayer      ((Player)      who);
		else if(who instanceof LivingEntity) out = new WrappedLivingEntity((LivingEntity)who);
		else                                 out = new WrappedEntity      (              who);
		
		return out;
	}
	
	public boolean HasWrapped(Entity who) {
		for(GameObject go : gameObjects) if(go instanceof WrappedEntity && ((WrappedEntity)go).entity == who) return true;
		return false;
	}
	
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent event) {
		Wrap(event.getPlayer());
	}
	
	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent event) {
		if(HasWrapped(event.getPlayer())) Destroy(Wrap(event.getPlayer()));
	}
	
	@EventHandler
	public void OnEntityDeath(EntityDeathEvent event) {
		if(HasWrapped(event.getEntity())) Destroy(Wrap(event.getEntity()));
	}
	
	public void Instantiate(GameObject go) {
		if(gameObjects.contains(go)) return;
		
		gameObjects.add(go);
		
		try {
			for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c)._Awake();
		} finally {
			for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c)._SetEnabled(true); //Default enable components
		}
	}
	
	public void Destroy(GameObject go) {
		if(!gameObjects.contains(go)) return;
		
		gameObjects.remove(go);
		
		try {
			if(go instanceof WrappedPlayer) {
				WrappedPlayer w = (WrappedPlayer)go;
				w.SavePersistent();
			} else {
				for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c)._SetEnabled(false);
				for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c).OnDestroy();
			}
		} catch(IOException e) {
			RmApisPlugin.INSTANCE.logger.warning("Failed to save Unitylike data for player "+((WrappedPlayer)go).player.getDisplayName());
			e.printStackTrace();
		} finally {
			gameObjects.remove(go);
		}
	}
	
	public UnitylikeObject FindObjectOfType(Class<? extends UnitylikeObject> clazz) {
		for(GameObject go : gameObjects) {
			if(clazz.isAssignableFrom(go.getClass())) return go;
			for(Component c : go.GetComponents(Component.class)) if(clazz.isAssignableFrom(c.getClass())) return c;
		}
		return null;
	}
	
	public UnitylikeObject[] FindObjectsOfType(Class<? extends UnitylikeObject> clazz) {
		ArrayList<UnitylikeObject> matches = new ArrayList<UnitylikeObject>();
		
		for(GameObject go : gameObjects) {
			if(clazz.isAssignableFrom(go.getClass())) matches.add(go);
			for(Component c : go.GetComponents(Component.class)) if(clazz.isAssignableFrom(c.getClass())) matches.add(c);
		}
		
		UnitylikeObject[] tmp = null;
		matches.toArray(tmp);
		return tmp;
	}
	
	@Override
	public void run() {

		//Register entities near players as WrappedEntity
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			for(Entity e : p.getNearbyEntities(entityAddRadius, entityAddRadius, entityAddRadius)) Wrap(e);
		}
		
		//Dispatch events
		
		Time.Update();
		
		for(GameObject go : gameObjects) for(Component c : go.GetComponents(JavaBehaviour.class)) {
			JavaBehaviour b = (JavaBehaviour)c;
			if(b.IsEnabled()) try { b.Update(); } catch(Throwable t) { t.printStackTrace(); }
		}
		
		for(GameObject go : gameObjects) for(Component c : go.GetComponents(JavaBehaviour.class)) {
			JavaBehaviour b = (JavaBehaviour)c;
			if(b.IsEnabled()) try { b.LateUpdate(); } catch(Throwable t) { t.printStackTrace(); }
		}
		
		ArrayList<JavaBehaviour> renderables = new ArrayList<JavaBehaviour>();
		for(GameObject go : gameObjects) for(Component c : go.GetComponents(JavaBehaviour.class)) renderables.add((JavaBehaviour)c);
		renderables.sort(new Comparator<JavaBehaviour>() {
			@Override
			public int compare(JavaBehaviour o1, JavaBehaviour o2) { return o1.GetRenderOrder()-o2.GetRenderOrder(); }
		});
		for(JavaBehaviour c : renderables) if(c.IsEnabled()) try { c._Render(); } catch(Throwable t) { t.printStackTrace(); }
		
		
	}

	public File getDataFolder() throws IOException {
		File out = new File(RmApisPlugin.INSTANCE.getDataFolder(), "unitylike-players");
		if(!out.exists()) out.mkdirs();
		return out;
	}
}
