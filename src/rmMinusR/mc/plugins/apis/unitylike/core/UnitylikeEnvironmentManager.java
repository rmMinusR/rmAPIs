package rmMinusR.mc.plugins.apis.unitylike.core;

import java.util.ArrayList;
import java.util.Comparator;

import org.bukkit.Bukkit;

import rmMinusR.mc.plugins.apis.RmApisPlugin;

public final class UnitylikeEnvironmentManager implements Runnable {
	
	private ArrayList<GameObject> gameObjects;
	
	public UnitylikeEnvironmentManager() {
		gameObjects = new ArrayList<GameObject>();
	}
	
	private int taskID;
	public void OnEnable() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RmApisPlugin.INSTANCE, this, 1, 1);
		Time.Init();
	}
	
	public void OnDisable() {
		Bukkit.getScheduler().cancelTask(taskID);
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
			for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c)._SetEnabled(false);
			for(Component c : go.GetComponents(JavaBehaviour.class)) ((JavaBehaviour)c).OnDestroy();
		} finally {
			gameObjects.remove(go);
		}
	}
	
	@Override
	public void run() {
		
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
}
