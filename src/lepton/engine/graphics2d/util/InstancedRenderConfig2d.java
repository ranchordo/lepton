package lepton.engine.graphics2d.util;

import lepton.engine.rendering.GObject;
import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.TextureImage;

public class InstancedRenderConfig2d {
	public Shader shader;
	public TextureImage image;
	public GObject geo;
	public InstanceAccumulator instanceAccumulator;
	public InstancedRenderConfig2d(Shader s, TextureImage i, GObject g, int objectSize, String ssbo_name) {
		shader=s;
		image=i;
		geo=g;
		if(objectSize>0 && ssbo_name!=null) {
			instanceAccumulator=new InstanceAccumulator(shader,objectSize,ssbo_name);
		}
	}
	@Override	
	public int hashCode() {
		return (shader==null?0:shader.hashCode())+(image==null?0:image.hashCode())+(geo==null?0:geo.hashCode());
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof InstancedRenderConfig2d) {
			if(((InstancedRenderConfig2d)o).shader==this.shader) {
				if(((InstancedRenderConfig2d)o).geo==this.geo) {
					if(((InstancedRenderConfig2d)o).image==this.image) {
						return true;
					}
				}
			}
		}
		return false;
	}
	@Override
	public String toString() {
		return "Shader name: "+shader.getFname()+", TextureImage: "+image+", GObject: "+geo;
	}
}
