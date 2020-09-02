package rmMinusR.mc.plugins.apis.unitylike.core;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.unitylike.wrapping.WrappedPlayer;

public final class UnitylikeEnvironment implements Runnable, Listener, IGameObjectHolder {
	
	//Singleton pattern
	private static UnitylikeEnvironment _instance;
	public static UnitylikeEnvironment GetInstance() { if(_instance == null) _instance = new UnitylikeEnvironment(); return _instance; }
	public static void _OnEnable() { GetInstance().OnEnable(); }
	public static void _OnDisable() { if(_instance != null) { _instance.OnDisable(); _instance = null; } }
	
	private UnitylikeEnvironment() {
		loadedScenes = new HashSet<Scene>();
		globalGameObjects = new HashSet<GameObject>();
	}
	
	/*package*/ Set<Scene> loadedScenes;
	/*package*/ Set<GameObject> globalGameObjects;
	
	//Enable/disable actions
	private boolean enabled = false;
	
	private void OnEnable() {
		//State check, don't double-register
		if(enabled) throw new IllegalStateException("Already enabled!");
		enabled = true;
		
		//Register for event handling
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
		//Start ticker
		tickerTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RmApisPlugin.INSTANCE, this, 1, 1);
		Time.Init();
		
		for(World w : Bukkit.getWorlds()) Scene.GetOrNew(w);
		for(Player p : Bukkit.getOnlinePlayers()) WrappedPlayer.GetOrNew(p);
	}
	
	private void OnDisable() {
		//State check, don't double-disable
		if(!enabled) throw new IllegalStateException("Already disabled!");
		enabled = false;
		
		//Unregister from event handling
		HandlerList.unregisterAll(this);
		//Disable ticker
		Bukkit.getScheduler().cancelTask(tickerTaskID);
		
		//Serialize all scenes
		for(Scene s : loadedScenes) OnWorldUnload(new WorldUnloadEvent(s.ref));
		loadedScenes.clear(); //GC hint
	}
	
	//World load/unload
	
	@EventHandler
	public void OnWorldLoad(WorldLoadEvent w) { Scene.GetOrNew(w.getWorld()); }
	
	@EventHandler
	public void OnWorldUnload(WorldUnloadEvent w) { if(Scene.IsLoaded(w.getWorld())) Scene.GetOrNew(w.getWorld()).Save(); }
	
	public File getDataFolder() {
		return new File(RmApisPlugin.INSTANCE.getDataFolder(), "objects");
	}
	
	//Player leave/join
	
	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e) { WrappedPlayer.GetOrNew(e.getPlayer()); }
	
	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent e) { if(WrappedPlayer.Get(e.getPlayer()) != null) Destroy(WrappedPlayer.Get(e.getPlayer())); }
	
	//Ticker
	
	private int tickerTaskID;
	
	@Override
	public void run() {
		Time.Update();
		for(Scene s : loadedScenes) s.Update();
		for(Scene s : loadedScenes) s.PhysicsUpdate();
		for(Scene s : loadedScenes) s.Render();
	}
	
	//GameObject management
	
	@Override
	public Collection<GameObject> GetGameObjects() {
		Set<GameObject> out = new HashSet<GameObject>();
		out.addAll(globalGameObjects);
		for(Scene s : loadedScenes) out.addAll(s.GetGameObjects());
		return out;
	}

	@Override
	public <T extends GameObject> T Instantiate(T go) {
		if(!globalGameObjects.contains(go)) {
			globalGameObjects.add(go);
			go._Awake();
		}
		return go;
	}

	@Override
	public <T extends GameObject> void Destroy(T go) {
		if(globalGameObjects.contains(go)) {
			globalGameObjects.remove(go);
			go._Destroy();
		}
	}
	
}
