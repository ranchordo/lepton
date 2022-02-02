package lepton.engine.rendering.pipelines;

import java.util.ArrayList;

import lepton.engine.rendering.FrameBuffer;
import lepton.util.advancedLogger.Logger;

public abstract class RenderPipelineElement {
	public static final String[] DEFAULT_COMPS={"main"};
	public static class OutHook {
		public byte inIndex;
		public byte outIndex;
		public RenderPipelineElement element;
		public String inName;
		public String outName;
		public String eleName;
		public OutHook(String i, String o, String e) {
			inName=i; outName=o; eleName=e;
		}
		private final void process(RenderPipelineElement rpe) {
			byte i;
			for(i=0;i<rpe.compNames().length;i++) {
				if(rpe.compNames()[i].equals(outName)) {
					outIndex=i;
					break;
				}
			}
			if(i==rpe.compNames().length) {
				Logger.log(4,"Pipeline: \""+outName+"\" is not a valid output for this element.");
			}
			RenderPipelineElement e=rpe.pipeline.getElements().get(eleName);
			element=e;
			if(e==null) {
				Logger.log(4,"Pipeline: \""+eleName+"\" is not a valid element.");
			}
			for(i=0;i<e.compNames().length;i++) {
				if(e.compNames()[i].equals(inName)) {
					inIndex=i;
					break;
				}
			}
			if(i==e.compNames().length) {
				Logger.log(4,"Pipeline: \""+outName+"\" is not a valid input for the target element \""+eleName+"\".");
			}
			
			
			//Run a few checks on the link
			if(rpe.pipeline!=e.pipeline) {
				Logger.log(4,"Somehow, the two elements "+rpe.name+" and "+e.name+" don't belong to the same pipeline.");
			}
			if(rpe.format()!=e.format()) {
				Logger.log(4,"The two elements "+rpe.name+" and "+e.name+" don't have the correct corresponding in/out formats.");
			}
			if(rpe.MS()!=e.MS()) {
				Logger.log(4,"The two elements "+rpe.name+" and "+e.name+" don't have the correct corresponding in/out MSAA values.");
			}
		}
	}
	protected final void processAllHooks() {
		for(OutHook o : outs) {
			o.process(this);
		}
	}
	protected final FrameBuffer generateStartingFramebuffer() {
		return new FrameBuffer(MS(),compNames().length,format());
	}
	private boolean starting=false;
	public final void setAsStartingElement() {
		starting=true;
	}
	private ArrayList<OutHook> outs=new ArrayList<OutHook>();
	public ArrayList<OutHook> getOutHooks() {
		return outs;
	}
	public final void hookTo(String outName,String eleName,String inName) {
		outs.add(new OutHook(inName,outName,eleName));
	}
	private String name="ERROR";
	public final String getName() {
		return name;
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
	public abstract String[] compNames_back();
	public final String[] compNames() {String[] o=compNames_back(); return o==null?DEFAULT_COMPS:o;}
	public abstract int MS();
	public abstract int format();
	public abstract boolean safeElement();
	private RenderPipeline pipeline;
	private boolean inited=false;
	protected void execute() {
		if(!inited) {
			init_back();
		}
		run_back();
	}
	public RenderPipelineElement(String name) {
		this.name=name;
		timeprofilername=name;
	}
	public final void setPipeline(RenderPipeline p) {
		if(pipeline!=null) {
			pipeline.getElements().remove(this.name);
			if(starting) {
				pipeline.startingElement=null;
			}
			pipeline.invalid=false;
		}
		pipeline=p;
		if(starting) {
			pipeline.startingElement=this;
		}
	}
	public final void remove() {
		pipeline.getElements().remove(this.name);
		pipeline.invalid=true;
	}
	private FrameBuffer buffer;
	public final FrameBuffer getBuffer() {return buffer;}
	protected final void setBuffer(FrameBuffer buffer) {this.buffer=buffer;}
	public abstract void run_back();
	public abstract void init_back();
}