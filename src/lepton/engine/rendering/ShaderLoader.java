package lepton.engine.rendering;

import java.util.HashMap;

public class ShaderLoader {
	public HashMap<String,Shader> shaders=new HashMap<String,Shader>();
	public Shader load(String fname) {
		if(shaders.containsKey(fname)) {
			return shaders.get(fname);
		} else {
			Shader s=new Shader(fname);
			shaders.put(fname,s);
			return s;
		}
	}
}