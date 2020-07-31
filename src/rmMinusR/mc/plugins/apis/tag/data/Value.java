package rmMinusR.mc.plugins.apis.tag.data;

public abstract class Value {
	public static Value fromString(String s) {
		if(s.charAt(0) == ValueNumber.IDENTIFIER) return ValueNumber.deserialize(s);
		if(s.charAt(0) == ValueString.IDENTIFIER) return ValueString.deserialize(s);
		return null;
	}
	
	public abstract String serialize();
	public abstract Value clone();
}
