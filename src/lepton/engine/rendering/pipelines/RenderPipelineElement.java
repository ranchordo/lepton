package lepton.engine.rendering.pipelines;

import java.util.HashMap;

import lepton.engine.rendering.FrameBuffer;

public abstract class RenderPipelineElement {
	public static class OutHook {
		protected OutHook(RenderPipelineElement e, int i) {
			this.element=e;
			this.index=i;
		}
		protected OutHook(String targetElement, String targetInput) {
			this.targetElement=targetElement;
			this.targetInput=targetInput;
		}
		public String targetElement;
		public String targetInput;
		public RenderPipelineElement element;
		public int index;
	}
	private HashMap<String,Integer> ins=new HashMap<String,Integer>();
	private HashMap<String,Integer> outs=new HashMap<String,Integer>();
	private String name="ERROR";
	public String getName() {
		return name;
	}
	private OutHook[] outHooks;
	public OutHook[] getOutHooks() {
		return outHooks;
	}
	public void processHooks() {
		for(OutHook h : outHooks) {
			RenderPipelineElement element=pipeline.getElements().get(h.targetElement);
			int inindex=element.ins.get(h.targetInput);
			h.targetElement=null;
			h.targetInput=null;
			h.element=element;
			h.index=inindex;
		}
	}
	public void hookTo(String thisoutput, String targetElement, String targetInput) {
		int outindex=outs.get(thisoutput);
		outHooks[outindex]=new OutHook(targetElement,targetInput);
	}
	public abstract String[] inputNames();
	public abstract String[] outputNames();
	private RenderPipeline pipeline;
	protected RenderPipelineElement(RenderPipeline pipeline, String name) {
		outHooks=new OutHook[outputNames().length];
		this.pipeline=pipeline;
		for(int i=0;i<inputNames().length;i++) {
			this.ins.put(inputNames()[i],i);
		}
		for(int i=0;i<outputNames().length;i++) {
			this.outs.put(outputNames()[i],i);
		}
		this.name=name;
		pipeline.getElements().put(name,this);
	}
	public final void remove() {
		pipeline.getElements().remove(this.name);
	}
	public abstract int getNumInputs();
	public abstract int getNumOutputs();
	private FrameBuffer inputs;
	private FrameBuffer outputs;
	public final FrameBuffer getInputs() {return inputs;}
	public final FrameBuffer getOutputs() {return outputs;}
	public abstract void run_back();
	public abstract void init_back();
	private boolean inited=false;
	public final void run() {
		if(!inited) {
			init_back();
			inited=true;
		}
		//Inputs will be populated with the correct inputs when this runs, and outputs will be populated with discarded buffers from the pool.
		run_back();
	}
}
