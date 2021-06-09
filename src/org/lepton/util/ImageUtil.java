package org.lepton.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.lepton.util.advancedLogger.Logger;

/**
 * Util class for doing image operations. Operations are done with BufferedImages, and operations are cached. The caching system is a little confusing, but you shouldn't have to worry about it too much.
 * The one useful function here is getImage.
 */
public class ImageUtil {
	public static HashMap<ModOp, BufferedImage> modCache=new HashMap<ModOp, BufferedImage>();
	public static int modCacheID=0;
	public static int modCacheLimit=30;
	public BufferedImage bi;
	public static void clearCache() {
		modCache=new HashMap<ModOp, BufferedImage>();
		modCacheID=0;
	}
	public static void limitCache() {
		if(modCacheLimit>0) {
			while(modCache.size()>modCacheLimit) {
				ModOp m=new ModOp(-1);
				for(Entry<ModOp,BufferedImage> e : modCache.entrySet()) {
					if(e.getKey().id>m.id) {
						m=e.getKey();
					}
				}
				modCache.remove(m);
			}
		}
	}
	/**
	 * Kind of like the "frontend" for the rehue op. This will check cache and only rehue if not cached.
	 */
	public static BufferedImage rehue(BufferedImage a, float h, float sm, float vm, boolean cache) {
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return rehue_op(a,h,sm,vm);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,h,sm,vm,modCacheID);
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	public static BufferedImage recolor(BufferedImage a, Color col, boolean cache) {
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return recolor_op(a,col);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,col,modCacheID);
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	public static BufferedImage realpha(BufferedImage a, float mul, boolean cache) {
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return realpha_op(a,mul);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,mul,modCacheID);
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	
	public static BufferedImage rehue(BufferedImage a, float h, float sm, float vm, boolean cache, String nm) {//Kind of like the "frontend" for the rehue op. This will check cache and only rehue if not cached.
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return rehue_op(a,h,sm,vm);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,h,sm,vm,modCacheID);
			m.nm=nm;
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	public static BufferedImage recolor(BufferedImage a, Color col, boolean cache, String nm) {
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return recolor_op(a,col);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,col,modCacheID);
			m.nm=nm;
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	public static BufferedImage realpha(BufferedImage a, float mul, boolean cache, String nm) {
		limitCache();
		if(modCacheLimit<0 || !cache) {
			return realpha_op(a,mul);
		} else { //Caching is enabled
			ModOp m=new ModOp(a,mul,modCacheID);
			m.nm=nm;
			modCacheID++;
			return handleCaching(a,m);
		}
	}
	private static BufferedImage handleCaching(BufferedImage a, ModOp m) {
		if(modCache.get(m)!=null) {
			//We have a cache hit!
			return modCache.get(m);
		} else {
			//Caching enabled, but this isn't there.
			BufferedImage b=null;
			switch(m.type) {
			case 0:
				b=rehue_op(a,m.h,m.sm,m.vm);
				break;
			case 1:
				b=recolor_op(a,m.col);
				break;
			case 2:
				b=realpha_op(a,m.mul);
			}
			modCache.put(m,b);
			return b;
		}
	}
	public static BufferedImage rehue_op(BufferedImage a, float h, float sm, float vm) { //Rehue an image. Convert each pixel to HSB, do some stuff, convert it back.
		ColorModel cm=a.getColorModel();
		boolean isAlphaPremultiplied=a.isAlphaPremultiplied();
		WritableRaster sub_raster=a.copyData(a.getRaster().createCompatibleWritableRaster());
		BufferedImage c=new BufferedImage(cm,sub_raster,isAlphaPremultiplied,null);
		WritableRaster raster=c.getRaster();
		for(int x=0;x<c.getWidth();x++) {
			for(int y=0;y<c.getHeight();y++) {
				int[] pix=raster.getPixel(x,y,(int[])null);
				float[] hsb=Color.RGBtoHSB(pix[0],pix[1],pix[2],null);

				if(h!=-1) {hsb[0]=h;}
				hsb[1]=hsb[1]*sm;
				hsb[2]=hsb[2]*vm;
				
				if(hsb[1]>1) {
					hsb[1]=1;
				}
				if(hsb[2]>1) {
					hsb[2]=1;
				}

				int rgb=Color.HSBtoRGB(hsb[0],hsb[1],hsb[2]);
				int r=((rgb>>16)&0xFF);
				int g=((rgb>>8)&0xFF);
				int b=(rgb&0xFF);
				pix[0]=r;
				pix[1]=g;
				pix[2]=b;
				raster.setPixel(x,y,pix);
			}
		}
		return c;
	}
	public static BufferedImage recolor_op(BufferedImage a, Color col) { //Replace all the colors in a with a specific color.  Keep the alpha.
		ColorModel cm=a.getColorModel();
		boolean isAlphaPremultiplied=a.isAlphaPremultiplied();
		WritableRaster sub_raster=a.copyData(a.getRaster().createCompatibleWritableRaster());
		BufferedImage c=new BufferedImage(cm,sub_raster,isAlphaPremultiplied,null);
		WritableRaster raster=c.getRaster();
		for(int x=0;x<c.getWidth();x++) {
			for(int y=0;y<c.getHeight();y++) {
				int[] pix=raster.getPixel(x,y,(int[])null);
				pix[0]=col.getRed();
				pix[1]=col.getGreen();
				pix[2]=col.getBlue();
				raster.setPixel(x,y,pix);
			}
		}
		return c;
	}
	public static BufferedImage realpha_op(BufferedImage a, float mul) { //Multiply the alpha by mul.
		ColorModel cm=a.getColorModel();
		boolean isAlphaPremultiplied=a.isAlphaPremultiplied();
		WritableRaster sub_raster=a.copyData(a.getRaster().createCompatibleWritableRaster());
		BufferedImage c=new BufferedImage(cm,sub_raster,isAlphaPremultiplied,null);
		WritableRaster raster=c.getRaster();
		for(int x=0;x<c.getWidth();x++) {
			for(int y=0;y<c.getHeight();y++) {
				int[] pix=raster.getPixel(x,y,(int[])null);
				pix[3]=(int) ((float)pix[3]*mul);
				raster.setPixel(x,y,pix);
			}
		}
		return c;
	}
	/**
	 * Gets an image from an inputStream
	 * @param stream
	 * @return BufferedImage
	 * @throws FileNotFoundException
	 */
	public static BufferedImage getImage(InputStream stream) throws FileNotFoundException { //Just get a bufferedimage.
		try {
			return ImageIO.read(stream);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
			return null;
		}
	}
	/**
	 * Like {@link org.hpjmci.util.ImageUtil.getImage}, but with functionality to obtain the inputstream itself. Fname would be something like "image" or "image integrated", and ext would be ".jpg". " integrated" will be used to determine whether or not the file is packaged within the extra source folder within the jarfile.
	 * Uses {@link org.hpjmci.util.Util.getOptionallyIntegratedStream} internally.
	 */
	public static BufferedImage getImage_fname(String fname, String ext) throws FileNotFoundException {
		return getImage(Util.getOptionallyIntegratedStream(fname,ext));
	}
	public static BufferedImage getImage_handleNotFound(InputStream stream) { //Just get a bufferedimage.
		try {
			return getImage(stream);
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
			return null;
		}
	}
}
