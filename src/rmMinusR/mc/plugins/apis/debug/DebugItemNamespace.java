package rmMinusR.mc.plugins.apis.debug;

public final class DebugItemNamespace {
	
	public static final String namespacePrefix = "rmapis:";
	
	public static void RegisterItems() {
		
		RodOfRaycasting.Register();
		TimeflowBand.Register();
		
	}
	
	public static String Localize(String itemID) {
		String out = itemID;
		if(!out.startsWith(namespacePrefix)) out = namespacePrefix+out;
		return "item."+out.replaceAll(":", ".").toLowerCase();
	}
	
}
