package com.github.ranchordo.lepton.engine.rendering.instanced;

import com.github.ranchordo.lepton.engine.rendering.GObject;
import com.github.ranchordo.lepton.engine.rendering.Shader;
import com.github.ranchordo.lepton.engine.rendering.Texture;

public class InstancedRenderConfig {
	public Shader shader;
	public Texture tex;
	public GObject geo;
	public InstanceAccumulator instanceAccumulator;
	public InstancedRenderConfig(Shader s, Texture i, GObject g, int objectSize, String ssbo_name) {
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
		if(o instanceof InstancedRenderConfig) {
			if(((InstancedRenderConfig)o).shader==this.shader) {
				if(((InstancedRenderConfig)o).geo.hashCode()==this.geo.hashCode()) {
					if(((InstancedRenderConfig)o).tex.hashCode()==this.tex.hashCode()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
