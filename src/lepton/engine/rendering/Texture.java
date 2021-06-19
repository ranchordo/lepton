package lepton.engine.rendering;

import java.io.FileNotFoundException;

import lepton.util.ImageUtil;
import lepton.util.Util;
import lepton.util.advancedLogger.Logger;

/**
 * Store some textures. Stores color, bump, and normal textures.
 */
public class Texture {
	public static final int COLOR=0;
	public static final int BUMP=1;
	public static final int NORMAL=2;
	
	public String name="";
	public boolean colorLoaded=false;
	public boolean bumpLoaded=false;
	public boolean normLoaded=false;
	TextureImage color; //Binding 0
	TextureImage bump; //Binding 1
	TextureImage norm; //Binding 2
	/**
	 * Reconstruct a texture based on another one. Effectively clones a texture object. Useful for cache separation. Shallow clone; buffers will still be linked.
	 * Use reset() to fix these link issues when separating a texture from a cache.
	 */
	public Texture(Texture in) {
		name=in.name;
		colorLoaded=in.colorLoaded;
		bumpLoaded=in.bumpLoaded;
		normLoaded=in.normLoaded;
		color=in.color;
		bump=in.bump;
		norm=in.norm;
	}
	public Texture() {
		color=new TextureImage(0);
		bump=new TextureImage(1);
		norm=new TextureImage(2);
	}
	/**
	 * Options are COLOR, BUMP, NORMAL. returns OpenGL-style int pointers to the texture buffers.
	 */
	public TextureImage get(int id) {
		switch(id) {
		case COLOR:
			return color;
		case BUMP:
			return bump;
		case NORMAL:
			return norm;
		default:
			return null;
		}
	}
	/**
	 * Reset a buffer. Options are COLOR, BUMP, NORMAL. Useful for separating a texture from a cache.
	 */
	public void reset(int id) {
		switch(id) {
		case COLOR:
			color=new TextureImage(0);
		case BUMP:
			bump=new TextureImage(1);
		case NORMAL:
			norm=new TextureImage(2);
		}
	}
	public void create_color(String fname, String ext) throws FileNotFoundException {
		color.create(ImageUtil.getImage(Util.getOptionallyIntegratedStream(fname,ext)));
	}
	public void create_bump(String fname, String ext) throws FileNotFoundException {
		bump.create(ImageUtil.getImage(Util.getOptionallyIntegratedStream(fname,ext)));
		bumpLoaded=true;
		Logger.log(0,"Found bump map "+fname);
	}
	public void create_norm(String fname, String ext) throws FileNotFoundException {
		norm.create(ImageUtil.getImage(Util.getOptionallyIntegratedStream(fname,ext)));
		normLoaded=true;
		Logger.log(0,"Found normal map "+fname);
	}
	public void bind() {
		color.bind();
		if(bumpLoaded) {bump.bind();}
		if(normLoaded) {norm.bind();}
	}
}
