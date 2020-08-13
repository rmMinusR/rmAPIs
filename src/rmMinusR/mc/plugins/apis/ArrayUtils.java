package rmMinusR.mc.plugins.apis;

import java.util.ArrayList;

public final class ArrayUtils {
	
	private ArrayUtils() {} //Disable instantiation
	
	public static <T> ArrayList<T> Slice(ArrayList<T> in, int ind_a, int ind_b, int ind_incr) {
		if(ind_incr == 0) return null;
		
		//Constrain indices and allow accessing Python-style
		while(ind_a < 0) ind_a += in.size(); while(ind_a >= in.size()) ind_a -= in.size();
		while(ind_b < 0) ind_b += in.size(); while(ind_b >= in.size()) ind_b -= in.size();
		
		int start_ind = (ind_incr>0)?Math.min(ind_a, ind_b):Math.max(ind_a, ind_b);
		
		//Build output array
		ArrayList<T> out = new ArrayList<T>();
		for(int i = start_ind; Math.min(ind_a, ind_b) <= i && i <= Math.max(ind_a, ind_b); i+= ind_incr) {
			out.add(in.get(i));
		}
		
		return out;
	}
	
}
