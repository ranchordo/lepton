package lepton.cpshlib;

import java.util.HashMap;

public class CPSHLoader {
	public HashMap<String,ComputeShader> shaders=new HashMap<String,ComputeShader>();
	public ComputeShader load(String fname) {
		if(shaders.containsKey(fname)) {
			return shaders.get(fname);
		} else {
			ComputeShader s=new ComputeShader(fname);
			shaders.put(fname,s);
			return s;
		}
	}
}
