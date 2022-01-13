package lepton.engine.rendering.pipelines;

import java.util.HashMap;

import lepton.engine.rendering.FrameBuffer;
import lepton.optim.objpoollib.PoolElement;
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
			if(h==null) {
				continue;
			}
			RenderPipelineElement element=pipeline.getElements().get(h.targetElement);
			if(element==null) {
				throw new IllegalArgumentException("Element "+h.targetElement+" not found.");
			}
			int inindex=element.ins.get(h.targetInput);
			h.targetElement=null;
			h.targetInput=null;
			h.element=element;
			h.index=inindex;
			h.element.expectedInputs[h.index]=true;
			if(h.element.inputFormat()!=this.outputFormat()) {
				Logger.log(4,"Connection between pipeline elements "+name+" and "+h.element.getName()+" did not share a common data format. ("+this.outputFormat()+", "+h.element.inputFormat()+")");
			}
			if(h.element.inputMS()!=this.outputMS()) {
				Logger.log(4,"Connection between pipeline elements "+name+" and "+h.element.getName()+" did not share a common multisampling factor. ("+this.outputFormat()+", "+h.element.inputFormat()+")");
			}
		}
	}
	protected final void checkStatus() {
		if(status==0) {
			for(int i=0;i<expectedInputs.length;i++) {
				if(expectedInputs[i]) {
					Logger.log(4,"Expected starting element of "+name+" had an input.");
				}
			}
		} else if(status==2) {
			for(OutHook oh : outHooks) {
				if(oh!=null) {
					Logger.log(4,"Expected ending element of "+name+" had an output.");
				}
			}
		}
	}
	private boolean executed=false;
	protected final void postexec() {
		if(!executed) {
			Logger.log(3,name+" never executed.");
		}
	}
	/**
	 * Pipeline element outputs can only hook to one other element input. No splitting by calling hookTo on the same output multiple times.
	 */
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
	private PoolElement<ColorBufferPool.ColorBuffer>[] filledInputs;
	private PoolElement<ColorBufferPool.ColorBuffer>[] outputColors;
	public final boolean[] getExpectedInputs() {
		return expectedInputs;
	}
	private boolean inited=false;
	public void fillBlankInputs() {
		for(int i=0;i<filledInputs.length;i++) {
			filledInputs[i]=pipeline.getColorBufferPool().alloc((inputMS()&0xFF)+(inputFormat()<<8));
		}
	}
	public void executeIfFilled() {
//		if(timeprofilerid>=0) {pipeline.getTimeProfiler().start(timeprofilerid);}
		for(int i=0;i<expectedInputs.length;i++) {
			if(expectedInputs[i] && (filledInputs[i]==null)) {
				return;
			}
		}
		if(executed) {
			Logger.log(4,name+" already executed.");
		}
		if(!inited) {
			init_back();
			inited=true;
		}
		for(int i=0;i<filledInputs.length;i++) {
			if(filledInputs[i]!=null) {
				filledInputs[i].o().attach(inputs,i);
			}
		}
		if(!onebuffer()) {
			for(int i=0;i<outHooks.length;i++) {
				outputColors[i]=pipeline.getColorBufferPool().alloc((outputMS()&0xFF)+(outputFormat()<<8));
				outputColors[i].o().attach(outputs,i);
			}
		}
		//Inputs will be populated with the correct inputs when this runs, and outputs will be populated with discarded buffers from the pool.
		System.out.println(name+": running with: ");
		for(PoolElement<ColorBufferPool.ColorBuffer> p : filledInputs) {
			System.out.println(p.o().tbo);
		}
		run_back();
		if(!onebuffer()) {
			for(PoolElement<ColorBufferPool.ColorBuffer> pe : filledInputs) {
				if(pe!=null) {
					pe.free();
				}
			}
		}
		executed=true;

		//Propagate the buffers down
		for(int i=0;i<outHooks.length;i++) {
			if(outHooks[i]==null) {
				(onebuffer()?filledInputs[i]:outputColors[i]).free();
				return;
			}
			outHooks[i].element.filledInputs[outHooks[i].index]=onebuffer()?filledInputs[i]:outputColors[i];
			outHooks[i].element.executeIfFilled();
		}
//		if(timeprofilerid>=0) {pipeline.getTimeProfiler().stop(timeprofilerid);}
	}
	protected final void reset() {
		for(int i=0;i<filledInputs.length;i++) {
			filledInputs[i]=null;
		}
		executed=false;
	}
	private RenderPipeline pipeline;
	protected byte status=-1;
	public boolean onebuffer() {
		return false;
	}
	/**
	 * Status: 0: starting, 1: intermediate, 2: ending.
	 */
	@SuppressWarnings("unchecked")
	public RenderPipelineElement(String name, byte status) {
		this.status=(byte)(status&0x03);
		if(onebuffer()&&(inputNames().length!=outputNames().length)) {
			throw new IllegalArgumentException("Different output/input lengths even though onebuffer() is true!");
		}
		expectedInputs=new boolean[inputNames().length];
		filledInputs=new PoolElement[inputNames().length];
		outputColors=new PoolElement[outputNames().length];
		outHooks=new OutHook[outputNames().length];
		for(int i=0;i<inputNames().length;i++) {
			this.ins.put(inputNames()[i],i);
		}
		for(int i=0;i<outputNames().length;i++) {
			this.outs.put(outputNames()[i],i);
		}
		this.name=name;
		timeprofilername=name;
		inputs=new FrameBuffer(inputMS(),-1,inputFormat());
		if(!onebuffer()) {
			outputs=new FrameBuffer(outputMS(),-1,outputFormat());
			outputs.setNumTexBuffers(outputNames().length);
		}
		inputs.setNumTexBuffers(inputNames().length);
	}
	public final void setPipeline(RenderPipeline p) {
		pipeline=p;
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
}
