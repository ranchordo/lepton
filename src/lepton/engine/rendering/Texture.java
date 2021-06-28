package lepton.engine.rendering;

import java.io.FileNotFoundException;
import java.util.HashMap;

import lepton.util.ImageUtil;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

/**
 * Store some textures. Stores color, bump, and normal textures.
 */
public class Texture {
	public static int ALBEDO=0;
	public static int NORMAL=1;
	public static int METALLIC=2;
	public static int ROUGHNESS=3;
	public static int AO=4;
	
	/**
	 * If you're gonna change this, you NEEEEEED to change this before loading any type of texture (that includes the loadOBJ methods). If you change this after things are loaded... I'm not sure what will happen to you.
	 * Correction: I'm not sure what will happen to you in the afterlife after your JVM kills you.
	 * This variable describes the number of texture unit things that this will use. Default is 5: albedo, normal, metallic, roughness, and AO. Change if passing different number of textures to shaders.
	 */
	public static int NUM_TEXTURES=5;
	/**
	 * For printing / logging purposes. Also please change this ASAP if you're gonna change this at all, like NUM_TEXTURES (but not quite as important).
	 */
	public static String[] tex_names=new String[] {"albedo","normal","metallic","roughness","AO"};
	/**
	 * Is Tan/Bitan data required for each corresponding texture? (default 0,1,0,0,0 [Index 1 is for normal maps, that's why we need tan/bitan])
	 */
	public static boolean[] TB_REQUIRED=new boolean[] {false,true ,false,false,false};
	
	public String name="";
	private TextureImage[] texImages=new TextureImage[5];
	private boolean[] loaded=new boolean[5];
	/**
	 * Given a hashmap of texture unit bindings, add the appropriate ones for textures. Uses tex_names to decide variable names.
	 * 
	 */
	public static void addRequiredUniformDefaults(HashMap<String,Integer> h) {
		for(int i=0;i<NUM_TEXTURES;i++) {
			h.put(tex_names[i],i);
		}
	}
	/**
	 * Reconstruct a texture based on another one. Effectively clones a texture object. Useful for cache separation. Shallow clone; buffers will still be linked.
	 * Use reset() to fix these link issues when separating a texture from a cache.
	 */
	public Texture(Texture in) {
		name=in.name;
		for(int i=0;i<NUM_TEXTURES;i++) {
			this.loaded[i]=in.loaded[i];
			this.texImages[i]=in.texImages[i];
		}
	}
	public Texture() {
		for(int i=0;i<NUM_TEXTURES;i++) {
			texImages[i]=new TextureImage(i);
			loaded[i]=false;
		}
	}
	/**
	 * Options are COLOR, BUMP, NORMAL. returns OpenGL-style int pointers to the texture buffers.
	 */
	public TextureImage get(int id) {
		return texImages[id];
	}
	/**
	 * Reset a buffer. Options are COLOR, BUMP, NORMAL. Useful for separating a texture from a cache.
	 */
	public void reset(int id) {
		texImages[id]=new TextureImage(id);
	}
	/**
	 * Make a texture image from a file
	 */
	public void create(int id, String fname, String ext) throws FileNotFoundException {
		texImages[id].create(ImageUtil.getImage(LeptonUtil.getOptionallyIntegratedStream(fname,ext)));
		loaded[id]=true;
		Logger.log(0,"Found "+tex_names[id]+" texture "+fname+".");
	}
	public void delete() {
		for(int i=0;i<NUM_TEXTURES;i++) {
			texImages[i].delete();
		}
	}
	/**
	 * Any texes loaded here?
	 */
	public boolean anyLoaded() {
		boolean ret=false;
		for(int i=0;i<NUM_TEXTURES;i++) {
			ret=ret||loaded[i];
		}
		return ret;
	}
	/**
	 * Is TB data required?
	 */
	public boolean TBReq() {
		boolean ret=false;
		for(int i=0;i<NUM_TEXTURES;i++) {
			ret=ret||(loaded[i]&&TB_REQUIRED[i]);
		}
		return ret;
	}
	public void bind() {
		for(int i=0;i<NUM_TEXTURES;i++) {
			if(loaded[i]) {
				texImages[i].bind();
			}
		}
	}
	/**
	 * Example: if albedo and normal textures are loaded it will return 3 (0b00011).
	 * Recommended for use in passing data about what's loaded to shaders. Done by default to uniform int "textureUse" (in GObject highRender_customTransform()).
	 * @return an int corresponding to what's loaded.
	 */
	public int loadedBitflag() {
		int ret=0;
		for(int i=0;i<NUM_TEXTURES;i++) {
			ret+=loaded[i]?1<<i:0;
		}
		return ret;
	}
}
