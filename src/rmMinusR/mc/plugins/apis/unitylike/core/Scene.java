package rmMinusR.mc.plugins.apis.unitylike.core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import de.tr7zw.nbtapi.NBTFile;
import rmMinusR.mc.plugins.apis.RmApisPlugin;
import rmMinusR.mc.plugins.apis.unitylike.Debug;
import rmMinusR.mc.plugins.apis.unitylike.data.Mathf;

public class Scene implements Listener, IGameObjectHolder {
	
	public final World ref;
	private Set<GameObject> gameObjects;
	
	//Factory methods
	
	public static Scene GetOrNew(World world) {
		Scene out = Get(world);
		if(out == null) {
			out = new Scene(world);
			UnitylikeEnvironment.GetInstance().loadedScenes.add(out);
		}
		return out;
	}
	
	public static Scene Get(World world) {
		for(Scene s : UnitylikeEnvironment.GetInstance().loadedScenes) if(s.ref.getUID().equals(world.getUID())) return s;
		return null;
	}
	
	public static boolean IsLoaded(World world) { return GetOrNew(world) != null; }
	
	protected Scene(World ref) {
		if(ref == null) throw new IllegalArgumentException("World reference cannot be null!");
		
		gameObjects = new HashSet<GameObject>();
		this.ref = ref;
		Bukkit.getPluginManager().registerEvents(this, RmApisPlugin.INSTANCE);
		
		for(Chunk c : ref.getLoadedChunks()) LoadChunk(c);
	}
	
	//Scene I/O
	
	public void Save() {
		HandlerList.unregisterAll(this);
		
		for(GameObject go : gameObjects) {
			try {
				for(Chunk c : ref.getLoadedChunks()) SaveChunk(c, true);
			} catch(Throwable e) {
				RmApisPlugin.INSTANCE.logger.warning("An error occurred while destroying "+go.getClass().getName());
				e.printStackTrace();
			}
		}
	}
	
	//Chunk I/O
	
	@EventHandler
	public void OnChunkLoad(ChunkLoadEvent e) {	if(e.getWorld().equals(ref)) LoadChunk(e.getChunk()); }
	
	@EventHandler
	public void OnChunkUnload(ChunkUnloadEvent e) { if(!e.getWorld().equals(ref)) SaveChunk(e.getChunk(), true); }
	
	@EventHandler
	public void OnWorldSave(WorldSaveEvent e) { for(Chunk c : ref.getLoadedChunks()) SaveChunk(c, false); }
	
	public Collection<GameObject> GetGameObjectsInChunk(int cx, int cz) {
		Set<GameObject> out = new HashSet<GameObject>();
		
		for(GameObject i : gameObjects) {
			ChunkCoordIntPair coords = i.GetTransform().GetPosition().ToBlockVector3().GetChunk();
			if(coords.getChunkX() == cx && coords.getChunkZ() == cz) out.add(i);
		}
		
		return out;
	}
	
	public void LoadChunk(Chunk c) {
		File[] contents = getChunkFolder(c).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".dat");
			}
		});
		
		if(contents == null || contents.length == 0) return; //Nothing to load
		
		for(File f : contents) {
			try {
				NBTFile inFile = new NBTFile(f);
				
				UnitylikeObject o = (UnitylikeObject) SerializationFactory.ReadFromNBT(inFile);
				
				if(o instanceof GameObject) gameObjects.add((GameObject)o); //Silent add
				else if(o instanceof Component) {
					Debug.LogError("Components on scenes are not yet supported");
				} else {
					Debug.LogError("Couldn't read type "+o.getClass());
				}
			} catch(IOException | ClassNotFoundException e) { Debug.LogError("Could not read artifact "+f.getName()); }
		}
	}
	
	public void SaveChunk(Chunk c, boolean doUnload) {
		Collection<GameObject> svq = GetGameObjectsInChunk(c.getX(), c.getZ());
		
		try {
			getChunkFolder(c).delete();
			getChunkFolder(c).mkdirs();
			
			for(GameObject i : svq) {
				if(!(i instanceof IPersistentSerializable)) continue;
				
				IPersistentSerializable s = (IPersistentSerializable) i;
				
				try {
					File objectFileLoc = new File(getChunkFolder(c), i.GetID()+".dat");
					NBTFile outFile = new NBTFile(objectFileLoc);
					
					SerializationFactory.WriteToNBT(s, outFile);
					
					outFile.save();
				} catch(IOException e) { Debug.LogError("Could not write artifact "+i); }
			}
		} finally {
			if(getChunkFolder(c).listFiles().length == 0) getChunkFolder(c).delete();
			
			if(doUnload) for(GameObject o : svq) gameObjects.remove(o); //Silent destroy
		}
	}
	
	//Ticker methods
	
	@SerializedName(value="@__isStarted")
	@Expose(serialize=true, deserialize=true)
	private boolean _isStarted = false;
	public void Start() {}
	protected final void _Update() {
		if(!_isStarted) {
			_isStarted = true;
			try {
				Start();
			} catch(Throwable t) { Debug.Log("Error calling Start() on "+this); t.printStackTrace(); }
		}
		
		try {
			Update();
		} catch(Throwable t) { Debug.Log("Error calling Update() on "+this); t.printStackTrace(); }
	}
	
	protected final void _PhysicsUpdate() {
		try {
			PhysicsUpdate();
		} catch(Throwable t) { Debug.Log("Error calling PhysicsUpdate() on "+this); t.printStackTrace(); }
	}
	
	public void Update() {
		for(GameObject o : gameObjects) o._Update();
	}

	public void PhysicsUpdate() {
		for(GameObject o : gameObjects) o._PhysicsUpdate();
	}

	public void Render() {
		Set<RenderDelegate> delegates = new HashSet<RenderDelegate>();
		for(IRenderable i : FindObjectsOfType(IRenderable.class)) delegates.addAll(i.Render());
		
		Map<Integer,Set<RenderDelegate>> sorted = new HashMap<Integer, Set<RenderDelegate>>();
		for(RenderDelegate i : delegates) {
			try {
				int prio = i.GetPriority();
				if(!sorted.containsKey(prio)) sorted.put(prio, new HashSet<RenderDelegate>());
				sorted.get(prio).add(i);
			} catch(Throwable t) { Debug.Log("Error calling GetPriority() on "+i); t.printStackTrace(); }
		}
		
		while(sorted.size() > 0) {
			int groupID = Mathf.Min(sorted.keySet());
			Set<RenderDelegate> group = sorted.get(groupID);
			for(RenderDelegate i : group) i._Render();
			sorted.remove(groupID);
		}
	}
	
	//GameObject management
	
	@Override
	public Collection<GameObject> GetGameObjects() {
		Set<GameObject> out = new HashSet<GameObject>();
		out.addAll(gameObjects);
		return out;
	}

	@Override
	public <T extends GameObject> T Instantiate(T go) {
		if(!gameObjects.contains(go)) {
			gameObjects.add(go);
			go._Awake();
		}
		return go;
	}

	@Override
	public <T extends GameObject> void Destroy(T go) {
		if(gameObjects.contains(go)) {
			gameObjects.remove(go);
			go._Destroy();
		}
	}

	public File getDataFolder() {
		return new File(UnitylikeEnvironment.GetInstance().getDataFolder(), ref.getName());
	}
	
	public File getChunkFolder(Chunk c) {
		return new File(getDataFolder(), c.getX()+","+c.getZ());
	}
	
}
