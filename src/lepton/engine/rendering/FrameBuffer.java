package lepton.engine.rendering;
import static org.lwjgl.opengl.GL46.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import lepton.util.LeptonUtil;
import lepton.util.advancedLogger.Logger;

/**
 * Can contain multiple color buffers, but can only contain one depth and stencil buffer. 
 */
public class FrameBuffer {
	public static final int FRAMEBUFFER=0, RENDERBUFFER=1, TEXTUREBUFFER=2;
	private int fbo;
	private int[] tbo;
	private int rbo;
	private int multiSample=-1;
	private int[] attachList;
	private int format;
	/**
	 * Get an int-style pointer to the framebuffer, renderbuffer, or texturebuffer object.
	 * id can either be FRAMEBUFFER, RENDERBUFFER, or TEXTUREBUFFER. secID can be anything unless you're fetching TEXTUREBUFFER, in which case it is the index of the texturebuffer you want to access.
	 */
	public int getID(int id, int secID) {
		switch(id) {
		case FRAMEBUFFER:
			return fbo;
		case RENDERBUFFER:
			return rbo;
		case TEXTUREBUFFER:
			return tbo[secID];
		default:
			throw new IllegalArgumentException("Please just use a valid buffer id.");
		}
	}
	/**
	 * ms is multisample. For MSAA (MultiSample AntiAliasing, used to remove jagged edges).
	 */
	public FrameBuffer(int ms) {
		this(ms,1,GL_RGBA16F);
	}
	public void setNumTexBuffers(int ntbo) {
		if(!flexibleBuffers) {
			Logger.log(4,"Tried to get the raw TBO list without a flexible buffer config. Init with ntbo=-1.");
		}
		if(tbo!=null) {
			if(tbo.length==ntbo) {
				return; //Nothing changed, why wipe everything?
			}
		}
		tbo=new int[ntbo];
		attachList=new int[ntbo];
		for(int i=0;i<ntbo;i++) {
			attachList[i]=GL_COLOR_ATTACHMENT0+i;
		}
	}
	public int[] getTBOs() {
		if(!flexibleBuffers) {
			Logger.log(4,"Tried to get the raw TBO list without a flexible buffer config. Init with ntbo=-1.");
		}
		return tbo;
	}
	private boolean flexibleBuffers=false;
	/**
	 * ms is multisample for MSAA (MultiSample AntiAliasing, used to remove jagged edges). ntbo is the Number of TBOs. Format is the storage format. Defaults to GL_RGBA16F. Other useful ones are GL_RGBA8
	 */
	public FrameBuffer(int ms, int ntbo, int format) {
		try(MemoryStack stack=MemoryStack.stackPush()){
			IntBuffer m=stack.mallocInt(1);
			glGetIntegerv(GL_MAX_COLOR_ATTACHMENTS,m);
			if(ntbo>m.get(0)) {
				throw new IllegalArgumentException("You just tried to init a framebuffer with "+ntbo+" attachments. Uh... Just so you know, the max is "+m.get(0));
			}
		}
		multiSample=ms;
		int texParam=(ms>0)?GL_TEXTURE_2D_MULTISAMPLE:GL_TEXTURE_2D;
		fbo=glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER,fbo);
		if(ntbo!=-1) {
			flexibleBuffers=true;
			tbo=new int[ntbo];
			attachList=new int[ntbo];
			for(int i=0;i<ntbo;i++) {
				tbo[i]=glGenTextures();
				glBindTexture(texParam,tbo[i]);
				if(ms>0) {
					glTexImage2DMultisample(texParam,ms,format,GLContextInitializer.winW,GLContextInitializer.winH,true);
				} else {
					glTexImage2D(texParam,0,format,GLContextInitializer.winW,GLContextInitializer.winH,0,GL_RGBA,GL_FLOAT,(FloatBuffer)null);
				}
				this.format=format;
				glTexParameteri(texParam,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
				glTexParameteri(texParam,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
				glTexParameteri(texParam,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
			    glTexParameteri(texParam,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
				
				glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0+i,texParam,tbo[i],0);
				attachList[i]=GL_COLOR_ATTACHMENT0+i;
			}
		}
		rbo=glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER,rbo); 
		if(ms>0) {
			glRenderbufferStorageMultisample(GL_RENDERBUFFER,ms,GL_DEPTH24_STENCIL8,GLContextInitializer.winW,GLContextInitializer.winH);
		} else {
			glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH24_STENCIL8,GLContextInitializer.winW,GLContextInitializer.winH);
		}
		
		glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_STENCIL_ATTACHMENT,GL_RENDERBUFFER,rbo);
		
//		rbo=glGenTextures();
//		glBindTexture(GL_TEXTURE_2D,tbo);
//		
//		glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH24_STENCIL8,GLContextInitializer.winW,GLContextInitializer.winH,0,GL_DEPTH_STENCIL,GL_UNSIGNED_INT_24_8,(FloatBuffer)null);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER)==GL_FRAMEBUFFER_COMPLETE) {
			Logger.log(0,"Framebuffer initialized successfully.");
		} else {
			Logger.log(4,"Framebuffer initiation did not result in a FRAMEBUFFER_COMPLETE flag.");
		}
		glBindTexture(texParam,0);
		glBindFramebuffer(GL_FRAMEBUFFER,0);
		glBindRenderbuffer(GL_RENDERBUFFER,0);
	}
	/**
	 * Clean up this framebuffer from memory.
	 */
	public void delete() {
		glDeleteFramebuffers(fbo);
		glDeleteTextures(tbo);
		glDeleteRenderbuffers(rbo);
	}
	/**
	 * Bind tbos[id] to a texture.
	 */
	public void bindTexture(int id) {
		if(multiSample>0) {
			throw new IllegalStateException("Cannot bind MSAA framebuffer to texture.");
		}
		glActiveTexture(GL_TEXTURE0);
		bindTexture(id,0);
	}
	/**
	 * Bind tbos[id] to a texture on texture unit "binding".
	 */
	public void bindTexture(int id, int binding) {
		if(multiSample>0) {
			throw new IllegalStateException("Cannot bind MSAA framebuffer to texture.");
		}
		glActiveTexture(GL_TEXTURE0+binding);
		glBindTexture(GL_TEXTURE_2D,tbo[id]);
	}
	/**
	 * Blit (AKA copy) this framebuffer to another (this.tbos[0] to target.tbos[0]).
	 */
	public void blitTo(FrameBuffer buffer) {
		blitTo(buffer,0);
	}
	/**
	 * Blit (AKA copy) this framebuffer to another (this.tbos[id] to target.tbos[0]).
	 */
	public void blitTo(FrameBuffer buffer, int id) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, buffer.fbo);
		glDrawBuffers(buffer.attachList);
		glReadBuffer(GL_COLOR_ATTACHMENT0+id);
		glBlitFramebuffer(0,0,GLContextInitializer.winW,GLContextInitializer.winH,0,0,GLContextInitializer.winW,GLContextInitializer.winH,GL_COLOR_BUFFER_BIT,GL_NEAREST);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	/**
	 * Blit (AKA copy) this framebuffer to another (this.tbos[id] to target.tbos[targID]).
	 */
	public void blitTo(FrameBuffer buffer, int id, int targID) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, buffer.fbo);
		glDrawBuffer(GL_COLOR_ATTACHMENT0+targID);
		glReadBuffer(GL_COLOR_ATTACHMENT0+id);
		glBlitFramebuffer(0,0,GLContextInitializer.winW,GLContextInitializer.winH,0,0,GLContextInitializer.winW,GLContextInitializer.winH,GL_COLOR_BUFFER_BIT,GL_NEAREST);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	/**
	 * Bind this such that stuff renders to it.
	 */
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER,fbo);
		glDrawBuffers(attachList);
	}
	public static void unbind_all() {
		glBindFramebuffer(GL_FRAMEBUFFER,0);
	}
	public void unbind() {
		unbind_all();
	}
	/**
	 * Bind tbos[id] as an image. Useful for compute shaders.
	 */
	public void bindImage(int id) {
		bindImage(id,GL_READ_WRITE,0);
	}
	/**
	 * Bind tbos[id] as an image to image unit "unit". Useful for compute shaders. rwmode is GL_READ_WRITE, GL_WRITE_ONLY, and GL_READ_ONLY.
	 */
	public void bindImage(int id, int rwmode, int unit) {
		bindTexture(id,unit);
		glBindImageTexture(unit,tbo[id],0,false,0,rwmode,format);
	}
}
