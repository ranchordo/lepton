package lepton.engine.rendering.instanced;

import lepton.engine.rendering.GObject;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.Texture;

public class InstancedRenderConfig3d {
	public Shader shader;
	public Texture tex;
	public GObject geo;
	public InstanceAccumulator instanceAccumulator;
	public InstancedRenderConfig3d(Shader s, Texture i, GObject g, int objectSize, String ssbo_name) {
		shader=s;
		tex=i;
		geo=g;
		if(ssbo_name!=null && objectSize>0) {
			instanceAccumulator=new InstanceAccumulator(shader,objectSize,ssbo_name);
		}
	}
	@Override
	public int hashCode() {
//		System.out.println(this.tex.hashCode());
		return (shader==null?0:shader.hashCode())+(tex==null?0:tex.hashCode())+(geo==null?0:geo.hashCode());
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof InstancedRenderConfig3d) {
			if(((InstancedRenderConfig3d)o).shader==this.shader) {
				if(((InstancedRenderConfig3d)o).geo.hashCode()==this.geo.hashCode()) {
					if(((InstancedRenderConfig3d)o).tex.hashCode()==this.tex.hashCode()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
