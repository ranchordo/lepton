package com.github.ranchordo.lepton.engine.rendering;

import static org.lwjgl.opengl.GL46.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import com.github.ranchordo.lepton.engine.util.Deletable;

/**
 * Backend OpenGL texture storage object. You shouldn't have to use this.
 */
public class TextureImage extends Deletable {
	private int id;
	public int binding;
	public int width;
	public int height;
	private ByteBuffer pixels;
	private BufferedImage bi=null;
	public int hashCode=-1;
	
	private Graphics initBI(BufferedImage input) { //Initialize internal BufferedImage with an input BI
		bi=input;
		Graphics g=bi.getGraphics();
		width=bi.getWidth();
		height=bi.getHeight();
		return g;
	}
	@Override public int hashCode() {
		return (hashCode!=-1)?hashCode:0;
	}
	public void create(BufferedImage in) {
		this.initBI(in);
		this.applyBI();
	}
	public TextureImage(int b) {
		id=glGenTextures();
		binding=b;
		width=0;
		height=0;
		adrt();
	}
	private void applyBI() { //Convert the internal BI to a bytebuffer and upload
		//int[] pixels_raw=new int[width*height*4];
		int[] pixels_raw=bi.getRGB(0, 0, width, height, null, 0, width);
		pixels=BufferUtils.createByteBuffer(width*height*4);
		hashCode=-1;
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {
				int pixel=pixels_raw[i*height+j];
				hashCode+=pixel;
				pixels.put((byte)((pixel>>16)&0xFF));
				pixels.put((byte)((pixel>> 8)&0xFF));
				pixels.put((byte)((pixel    )&0xFF));
				pixels.put((byte)((pixel>>24)&0xFF));
			}
		}
		pixels.flip();
		
		upload();
		
	}
	public void delete() {
		glDeleteTextures(id);
		rdrt();
	}
	private void upload() {
		glActiveTexture(GL_TEXTURE0+binding);
		glBindTexture(GL_TEXTURE_2D,id);
		glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D,0);
	}
	public void bind() { //Bind it!
		glActiveTexture(GL_TEXTURE0+binding);
		glBindTexture(GL_TEXTURE_2D,id);
	}
}
