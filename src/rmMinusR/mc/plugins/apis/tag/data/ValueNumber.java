package rmMinusR.mc.plugins.apis.tag.data;

public class ValueNumber extends Value {
	
	public static char IDENTIFIER = '#';
	public double value;
	
	public ValueNumber(double value) {
		this.value = value;
	}
	
	public ValueNumber() {
		value = 0;
	}

	@Override
	public String serialize() {
		return String.valueOf(IDENTIFIER)+String.valueOf(value);
	}

	public static Value deserialize(String s) {
		return new ValueNumber(Double.parseDouble(s.replaceFirst(String.valueOf(IDENTIFIER), "")));
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
	public Value clone() {
		return new ValueNumber(value+0);
	}
}
