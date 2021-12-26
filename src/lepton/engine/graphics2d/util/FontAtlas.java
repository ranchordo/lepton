package lepton.engine.graphics2d.util;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.HashMap;

import lepton.engine.rendering.TextureImage;
import lepton.util.ImageUtil;
import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

public class FontAtlas {
	private BufferedImage atlas;
	public TextureImage textureImage;
	private int spacing;
	private int vertOffset;
	private HashMap<Integer,Glyph> glyphs=null;
	public String layout;
	private String name;
	protected FontAtlas(String fname, String ext, int spacing, int vertOffset, String layout) {
		try {
			atlas=ImageUtil.getImage(LeptonUtil.getOptionallyIntegratedStream(fname,ext));
		} catch (FileNotFoundException e) {
			Logger.log(4,e.toString(),e);
		}
		this.name=fname;
		this.spacing=spacing;
		this.vertOffset=vertOffset;
		this.layout=layout;
	}
	public int getSpacing() {
		return spacing;
	}
	public int getVertOffset() {
		return vertOffset;
	}
	public void clean() {
		textureImage.delete();
		glyphs.clear();
	}
	public void load() {
		glyphs=new HashMap<Integer,Glyph>();
		if(atlas==null) {
			Logger.log(4,"Atlas image is null. Whaddaya want me to do?");
			return;
		}
		int idx=0;
		int px=0;
		boolean pcont=false;
		for(int i=0;i<atlas.getWidth();i++) {
			boolean cont=false;
			for(int j=0;j<atlas.getHeight();j++) {
				int pixel=atlas.getRGB(i,j);
				int alpha=(pixel>>24)&0xFF;
				if(alpha>250) {
					cont=true;
					break;
				}
			}
			if(cont&&!pcont) {
				px=i;
			}
			if(pcont&&!cont) {
				Glyph g=new Glyph();
				g.start=px;
				g.end=i-1;
				int ch=(int)layout.charAt(idx);
				idx++;
				glyphs.put(ch,g);
			}
			pcont=cont;
		}
		textureImage=new TextureImage(0);
		textureImage.create(atlas);
		Logger.log(0,"Loaded font atlas "+name);
	}
	public int getWidth() {
		return atlas.getWidth();
	}
	public int getHeight() {
		return atlas.getHeight();
	}
	public Glyph get(char c) {
		return glyphs.get((int)c);
	}
}
