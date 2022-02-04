package com.github.ranchordo.lepton.engine.rendering.pipelines;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL43.*;

import java.nio.FloatBuffer;

import com.github.ranchordo.lepton.engine.rendering.FrameBuffer;
import com.github.ranchordo.lepton.engine.rendering.GLContextInitializer;
import com.github.ranchordo.lepton.optim.objpoollib.PoolElement;
import com.github.ranchordo.lepton.optim.objpoollib.VariedAbstractObjectPool;
import com.github.ranchordo.lepton.optim.objpoollib.VariedPoolInitCreator;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

public class ColorBufferPool extends VariedAbstractObjectPool<ColorBufferPool.ColorBuffer> {
	public static class ColorBuffer {
		public int tbo;
		public int ms;
		public int format;
		public ColorBuffer(int ms, int format) {
			this.ms=ms;
			this.format=format;
			int texParam=(ms>0)?GL_TEXTURE_2D_MULTISAMPLE:GL_TEXTURE_2D;
			tbo=glGenTextures();
			glBindTexture(texParam,tbo);
			if(ms>0) {
				glTexStorage2DMultisample(texParam,ms,format,GLContextInitializer.winW,GLContextInitializer.winH,true);
			} else {
				glTexStorage2D(texParam,1,format,GLContextInitializer.winW,GLContextInitializer.winH);
			}
			glTexParameteri(texParam,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
			glTexParameteri(texParam,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
			glTexParameteri(texParam,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
		    glTexParameteri(texParam,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
		    glBindTexture(texParam,0);
		}
		
		public void attach(FrameBuffer fbo, int i) {
			int texParam=(ms>0)?GL_TEXTURE_2D_MULTISAMPLE:GL_TEXTURE_2D;
			glBindFramebuffer(GL_FRAMEBUFFER,fbo.getID(FrameBuffer.FRAMEBUFFER,0));
			glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0+i,texParam,tbo,0);
			fbo.getTBOs()[i]=tbo;
			if(glCheckFramebufferStatus(GL_FRAMEBUFFER)!=GL_FRAMEBUFFER_COMPLETE) {
				Logger.log(4,"GL_FRAMEBUFFER_STATUS: "+glCheckFramebufferStatus(GL_FRAMEBUFFER));
			}
			glBindFramebuffer(GL_FRAMEBUFFER,0);
		}
		public void detach(FrameBuffer attached, int attachedb) {
			int texParam=(ms>0)?GL_TEXTURE_2D_MULTISAMPLE:GL_TEXTURE_2D;
			glBindFramebuffer(GL_FRAMEBUFFER,attached.getID(FrameBuffer.FRAMEBUFFER,0));
			glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0+attachedb,texParam,0,0);
			attached=null;
			attachedb=-1;
			glBindFramebuffer(GL_FRAMEBUFFER,0);
		}
		public void delete() {
			glDeleteTextures(tbo);
		}
	}
	
	
	
	public ColorBufferPool(String type) {
		super(type, new VariedPoolInitCreator<ColorBuffer>() {
			@Override
			public ColorBuffer allocateInitValueVaried(int desc) {
				return new ColorBuffer(desc&0xFF,desc>>8);
			}
		});
	}
	@Override
	public void handleDeletion(PoolElement<ColorBuffer> i) {
		i.o().delete();
	}
}
