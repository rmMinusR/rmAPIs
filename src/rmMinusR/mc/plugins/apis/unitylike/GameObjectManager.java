package rmMinusR.mc.plugins.apis.unitylike;

import java.util.ArrayList;
import java.util.Comparator;

import org.bukkit.Bukkit;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public final class GameObjectManager implements Runnable {
	
	private ArrayList<GameObject> gameObjects;
	
	public GameObjectManager() {
		gameObjects = new ArrayList<GameObject>();
	}
	
	private int taskID;
	public void OnEnable() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RmApisPlugin.INSTANCE, this, 1, 1);
	}
	
	public void OnDisable() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
	public void Instantiate(GameObject go) {
		if(gameObjects.contains(go)) return;
		
		gameObjects.add(go);
		
		try {
			for(BehaviourComponent c : go.GetComponents()) c._Awake();
		} finally {
			for(BehaviourComponent c : go.GetComponents()) c._SetEnabled(true); //Default enable components
		}
	}
	
	public void Destroy(GameObject go) {
		if(!gameObjects.contains(go)) return;
		
		gameObjects.remove(go);
		
		try {
			for(BehaviourComponent c : go.GetComponents()) c._SetEnabled(false);
			for(BehaviourComponent c : go.GetComponents()) c.OnDestroy();
		} finally {
			gameObjects.remove(go);
		}
	}
	
	@Override
	public void run() {
		
		for(GameObject go : gameObjects) for(BehaviourComponent c : go.GetComponents()) if(c.IsEnabled()) try { c.Update(); } catch(Throwable t) { t.printStackTrace(); }
		
		for(GameObject go : gameObjects) for(BehaviourComponent c : go.GetComponents()) if(c.IsEnabled()) try { c.LateUpdate(); } catch(Throwable t) { t.printStackTrace(); }
		
		ArrayList<BehaviourComponent> renderables = new ArrayList<BehaviourComponent>(); for(GameObject go : gameObjects) renderables.addAll(go.GetComponents());
		renderables.sort(new Comparator<BehaviourComponent>() {
			@Override
			public int compare(BehaviourComponent o1, BehaviourComponent o2) { return o1.GetRenderOrder()-o2.GetRenderOrder(); }
		});
		for(BehaviourComponent c : renderables) if(c.IsEnabled()) try { c._Render(); } catch(Throwable t) { t.printStackTrace(); }
		
	}
}
