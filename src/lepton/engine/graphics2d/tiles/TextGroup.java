package lepton.engine.graphics2d.tiles;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

import lepton.engine.graphics2d.util.FontAtlas;
import lepton.engine.graphics2d.util.Glyph;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.Texture;
import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.engine.rendering.instanced.InstancedRenderer;

public class TextGroup extends Tile2d {
	public static Shader textShader;
	public ArrayList<GenericInstancedTile2d> characters=new ArrayList<GenericInstancedTile2d>();
	private String s;
	private FontAtlas f;
	public float height;
	public float r;
	public float g;
	public float b;
	private InstanceAccumulator ia;
	public TextGroup(String str, FontAtlas font, float x, float y, float height, float r, float g, float b, InstancedRenderer sl) {
		f=font;
		s=str;
		refreshString=true;
		this.x=x;
		this.y=y;
		this.height=height;
		if(textShader==null) {
			textShader=GLContextInitializer.shaderLoader.load("specific/textChar");
		}
		ia=sl.loadConfiguration(textShader,new Texture(f.textureImage,0),Tile2d.genericSquare,12,"info_buffer").instanceAccumulator;
		this.r=r;
		this.g=g;
		this.b=b;
	}
	private boolean refreshString=false;
	public void setString(String s) {
		this.s=s;
		refreshString=true;
	}
	@Override
	public void logic() {
		float pix2ui=height/f.getHeight();
		if(refreshString) {
			int len=0;
			for(int i=0;i<s.length();i++) {
				if(f.get(s.charAt(i))!=null) {
					len++;
				}
			}
			while(len!=characters.size()) {
				if(len<characters.size()) {
					characters.remove(characters.size()-1);
				} else {
					GenericInstancedTile2d t=(GenericInstancedTile2d)new GenericInstancedTile2d(ia).setParent(parent);
					t.posMode=this.posMode;
					characters.add(t);
				}
			}
			Glyph a=f.get('I');
			int spacew=a.end-a.start;
			int offset=0;
			int diff=0;
			for(int i=0;i<s.length();i++) {
				Glyph g=f.get(s.charAt(i));
				if(g!=null) {
					GenericInstancedTile2d c=characters.get(i+diff);
					c.x=(offset*pix2ui)+x;
					c.y=y;
					c.height=height;
					c.width=pix2ui*(g.end-g.start);
					c.objectSize=GenericInstancedTile2d.defaultObjectSize+4;
//					c.image=f.textureImage;
					c.texX=g.start/(float)f.getWidth();
					c.texY=0;
					c.texW=(g.end-g.start)/(float)f.getWidth();
					c.texH=1;
//					c.renderingShader=textShader;
					c.refreshDataLength();
					c.additionalData[0]=this.r;
					c.additionalData[1]=this.g;
					c.additionalData[2]=this.b;
					c.additionalData[3]=0;
				} else {
					diff--;
				}
				int w=g==null?spacew:g.end-g.start;
				offset+=w;
				offset+=f.getSpacing();
			}
			refreshString=false;
		}
		runEventListeners();
	}
	@Override
	public void render() {
		for(GenericInstancedTile2d t : characters) {
			t.render();
		}
	}
	private Vector4f a=new Vector4f();
	@Override
	public Vector4f getBoundingBox() {
		if(characters.size()==0) {a.set(x,y,x,y); return a;}
		Vector4f bb0=characters.get(0).getBoundingBox();
		Vector4f bb1=characters.get(characters.size()-1).getBoundingBox();
		a.set(bb0.x,bb1.y,bb1.z,bb0.w);
		return a;
	}
	@Override
	public Tile2d setParent(Tile2d parent) {
		for(GenericInstancedTile2d c : characters) {
			c.setParent(parent);
		}
		this.parent=parent;
		return this;
	}
}
