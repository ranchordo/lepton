package com.github.ranchordo.lepton.engine.rendering.pipelineElements;

import java.util.ArrayList;

import org.lwjgl.opengl.GL15;

import com.github.ranchordo.lepton.cpshlib.ComputeShader;
import com.github.ranchordo.lepton.engine.rendering.pipelines.RenderPipelineElement;

public class GenericCPSHDispatch extends RenderPipelineElement {
	@FunctionalInterface
	public static interface DefaultUniformRoutine {
		public void run(ComputeShader shader);
	}
	public ArrayList<DefaultUniformRoutine> uniformRoutines=new ArrayList<DefaultUniformRoutine>();
	private ComputeShader cpsh;
	private int format;
	private int dx, dy, dz;
	public GenericCPSHDispatch(String name, ComputeShader c, int format, int dimX, int dimY, int dimZ) {
		super(name);
		cpsh=c;
		this.format=format;
		this.dx=dimX; this.dy=dimY; this.dz=dimZ;
	}
	
	@Override
	public void run_back() {
		getBuffer().bindImage(0);
		cpsh.bind();
		for(DefaultUniformRoutine r : uniformRoutines) {
			r.run(cpsh);
		}
		cpsh.applyAllSSBOs();
		cpsh.dispatch(dx,dy,dz);
	}

	@Override
	public void init_back() {
		//No init needed
	}

	@Override
	public String[] compNames_back() {
		return null;
	}

	@Override
	public int MS() {
		return 0;
	}

	@Override
	public int format() {
		return GL15.GL_RGBA8;
	}

	@Override
	public boolean safeElement() {
		// TODO Auto-generated method stub
		return false;
	}

}
