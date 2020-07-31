package rmMinusR.mc.plugins.apis.tag.data;

public class ValueString extends Value {
	
	public static char IDENTIFIER = '"';
	public String value;
	
	public ValueString(String value) {
		this.value = value;
	}
	
	public ValueString() {
		value = "";
	}

	@Override
	public String serialize() {
		return IDENTIFIER+value;
	}

	public static Value deserialize(String s) {
		return new ValueString(s.replaceFirst(String.valueOf(IDENTIFIER), ""));
	}
	
	@Override
	public String toString() {
		return "'"+value+"'";
	}
	
	public Value clone() {
		return new ValueString(value+"");
	}
}
