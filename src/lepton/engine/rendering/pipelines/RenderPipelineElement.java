package lepton.engine.rendering.pipelines;

import java.util.HashMap;

import lepton.engine.rendering.FrameBuffer;
import lepton.util.advancedLogger.Logger;

public abstract class RenderPipelineElement {
	public static final String[] DEFAULT_INPUTS={"input"};
	public static final String[] DEFAULT_OUTPUTS={"output"};
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
	public final String getName() {
		return name;
	}
	private OutHook[] outHooks;
	public final OutHook[] getOutHooks() {
		return outHooks;
	}
	private String timeprofilername;
	private int timeprofilerid=-1;
	protected final void setTimeprofilerID(int id) {
		timeprofilerid=id;
	}
	/**
	 * Setting two elements to the same name will make them appear in the same category.
	 */
	public final void setTimeProfilerName(String name) {
		timeprofilername=name;
	}
	public final String getTimeProfilerName() {
		return timeprofilername;
	}
	protected final void processHooks() {
		for(OutHook h : outHooks) {
			RenderPipelineElement element=pipeline.getElements().get(h.targetElement);
			int inindex=element.ins.get(h.targetInput);
			h.targetElement=null;
			h.targetInput=null;
			h.element=element;
			h.index=inindex;
			h.element.expectedInputs[h.index]=true;
		}
	}
	protected final void checkStatus() {
		if(status==0) {
			for(int i=0;i<expectedInputs.length;i++) {
				if(expectedInputs[i]) {
					Logger.log(4,"Expected starting element of "+name+" had an input.");
				}
			}
		}
	}
	private boolean executed=false;
	protected final void postexec() {
		if(!executed) {
			Logger.log(4,name+" never executed.");
		}
	}
	public final void hookTo(String thisoutput, String targetElement, String targetInput) {
		int outindex=outs.get(thisoutput);
		outHooks[outindex]=new OutHook(targetElement,targetInput);
	}
	public abstract String[] inputNames_back();
	public abstract String[] outputNames_back();
	public final String[] inputNames() {String[] o=inputNames_back(); return o==null?DEFAULT_INPUTS:o;}
	public final String[] outputNames() {String[] o=outputNames_back(); return o==null?DEFAULT_OUTPUTS:o;}
	public abstract int inputMS();
	public abstract int outputMS();
	public abstract int inputFormat();
	public abstract int outputFormat();
	private boolean[] expectedInputs;
	private boolean[] filledInputs;
	public final boolean[] getExpectedInputs() {
		return expectedInputs;
	}
	private void executeIfFilled() {
		for(int i=0;i<expectedInputs.length;i++) {
			if(expectedInputs[i]!=filledInputs[i]) {
				return;
			}
		}
		execute();
		propagate();
	}
	protected final void reset() {
		for(int i=0;i<filledInputs.length;i++) {
			filledInputs[i]=false;
		}
		executed=false;
	}
	protected final void propagate() {
		for(int i=0;i<outHooks.length;i++) {
			if(outHooks[i]==null) {return;}
			outputs.blitTo(outHooks[i].element.inputs,i,outHooks[i].index);
			outHooks[i].element.filledInputs[outHooks[i].index]=true;
			outHooks[i].element.executeIfFilled();
		}
	}
	private RenderPipeline pipeline;
	protected byte status=-1;
	/**
	 * Status: 0: starting, 1: intermediate, 2: ending.
	 */
	protected RenderPipelineElement(RenderPipeline pipeline, String name, byte status) {
		this.status=status;
		expectedInputs=new boolean[inputNames().length];
		filledInputs=new boolean[inputNames().length];
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
		timeprofilername=name;
		inputs=new FrameBuffer(inputMS(),inputNames().length,inputFormat());
		outputs=new FrameBuffer(outputMS(),outputNames().length,outputFormat());
	}
	public final void remove() {
		pipeline.getElements().remove(this.name);
	}
	private FrameBuffer inputs;
	private FrameBuffer outputs;
	public final FrameBuffer getInputs() {return inputs;}
	public final FrameBuffer getOutputs() {return outputs;}
	public abstract void run_back();
	public abstract void init_back();
	private boolean inited=false;
	protected final void execute() {
		if(executed) {
			Logger.log(4,name+" already executed.");
		}
		if(timeprofilerid>=0) {pipeline.getTimeProfiler().start(timeprofilerid);}
		if(!inited) {
			init_back();
			inited=true;
		}
		//Inputs will be populated with the correct inputs when this runs, and outputs will be populated with discarded buffers from the pool.
		run_back();
		if(timeprofilerid>=0) {pipeline.getTimeProfiler().stop(timeprofilerid);}
		executed=true;
	}
}
