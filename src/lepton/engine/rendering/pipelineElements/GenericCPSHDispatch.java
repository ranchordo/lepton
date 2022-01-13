package lepton.engine.rendering.pipelineElements;

import java.util.ArrayList;

import org.lwjgl.opengl.GL43;

import lepton.cpshlib.ComputeShader;
import lepton.engine.rendering.pipelines.RenderPipelineElement;

public class GenericCPSHDispatch extends RenderPipelineElement {
	@FunctionalInterface
	public static interface DefaultUniformRoutine {
		public void run(ComputeShader shader);
	}
	public ArrayList<DefaultUniformRoutine> uniformRoutines=new ArrayList<DefaultUniformRoutine>();
	private ComputeShader cpsh;
	private int format;
	private int dx, dy, dz;
	public GenericCPSHDispatch(String name, byte status, ComputeShader c, int format, int dimX, int dimY, int dimZ) {
		super(name, status);
		cpsh=c;
		this.format=format;
		this.dx=dimX; this.dy=dimY; this.dz=dimZ;
	}

	@Override
	public String[] inputNames_back() {
		return null;
	}

	@Override
	public String[] outputNames_back() {
		return null;
	}

	@Override
	public int inputMS() {
		return 0;
	}

	@Override
	public int outputMS() {
		return 0;
	}

	@Override
	public int inputFormat() {
		return format;
	}

	@Override
	public int outputFormat() {
		return format;
	}
	
	@Override
	public boolean onebuffer() {
		return true;
	}

	@Override
	public void run_back() {
		getInputs().bindImage(0);
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

}
