package lepton.engine.rendering.pipelines;

import static org.lwjgl.opengl.GL43.*;

import java.nio.FloatBuffer;

import lepton.engine.rendering.FrameBuffer;
import lepton.engine.rendering.GLContextInitializer;
import lepton.optim.objpoollib.PoolElement;
import lepton.optim.objpoollib.VariedAbstractObjectPool;
import lepton.optim.objpoollib.VariedPoolInitCreator;

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
