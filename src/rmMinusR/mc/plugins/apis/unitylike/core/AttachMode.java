package rmMinusR.mc.plugins.apis.unitylike.core;

public class AttachMode {
	
	private AttachMode() {} // prevent instantiation
	
	public static final int Global = 0b0001;
	public static final int Scene = 0b0010;
	public static final int Object = 0b0100;
	
	public static boolean CanAttachGlobal(int mode) {
		return (Global & mode) != 0;
	}
	
	public static boolean CanAttachToScene(int mode) {
		return (Scene & mode) != 0;
	}
	
	public static boolean CanAttachToObject(int mode) {
		return (Object & mode) != 0;
	}
	
}
