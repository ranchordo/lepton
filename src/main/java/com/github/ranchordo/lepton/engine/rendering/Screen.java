package com.github.ranchordo.lepton.engine.rendering;

import static org.lwjgl.opengl.GL46.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.github.ranchordo.lepton.util.LeptonUtil;

/**
 * Giant blindfold that covers your camera.
 */
public class Screen {
	int vbo;
	int tbo;
	public Screen() {
		vbo=glGenBuffers();
		tbo=glGenBuffers();
		float xx=(float) (2*(GLContextInitializer.aspectRatio)*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
		float xs=(float) (-2*(GLContextInitializer.aspectRatio)*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
		float yx=(float) (2*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
		float ys=(float) (-2*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
		float z=-2;
		float[] v_data=new float[] {
			xs,ys,z,
			xx,ys,z,
			xx,yx,z,
			
			xs,ys,z,
			xx,yx,z,
			xs,yx,z
		};
		float[] t_data=new float[] {
			0,0,
			1,0,
			1,1,
			
			0,0,
			1,1,
			0,1
		};
		glBindBuffer(GL_ARRAY_BUFFER,vbo);
		glBufferData(GL_ARRAY_BUFFER,v_data,GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER,tbo);
		glBufferData(GL_ARRAY_BUFFER,t_data,GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER,0);
	}
	float[] mma=new float[16];
	FloatBuffer fm=BufferUtils.createFloatBuffer(16);
	public void render() {
		glDisable(GL_DEPTH_TEST);
		glDepthMask(false);
		LeptonUtil.openGLMatrix(GLContextInitializer.proj_matrix,mma);
		fm=LeptonUtil.asFloatBuffer(mma,fm);
		GLContextInitializer.activeShader.setUniformMatrix4fv("proj_matrix",fm);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(8);
		glBindBuffer(GL_ARRAY_BUFFER,vbo);
		glVertexAttribPointer(0,3,GL_FLOAT,false,0,0);
		glBindBuffer(GL_ARRAY_BUFFER,tbo);
		glVertexAttribPointer(8,2,GL_FLOAT,false,0,0);
		glBindBuffer(GL_ARRAY_BUFFER,0);
		glDrawArrays(GL_TRIANGLES,0,2*3);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(8);
		glDepthMask(true);
		glEnable(GL_DEPTH_TEST);
	}
}
