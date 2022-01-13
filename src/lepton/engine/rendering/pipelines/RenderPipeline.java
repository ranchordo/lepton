package lepton.engine.rendering.pipelines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import lepton.engine.rendering.FrameBuffer;
import lepton.util.TimeProfiler;
import lepton.util.advancedLogger.Logger;

public class RenderPipeline {
	@FunctionalInterface
	public static interface PipelineAdditionRoutine {
		public RenderPipelineElement run();
	}
	private TimeProfiler timeprofiler;
	public TimeProfiler getTimeProfiler() {
		return timeprofiler;
	}
	private ColorBufferPool colorBufferPool;
	public ColorBufferPool getColorBufferPool() {
		return colorBufferPool;
	}
	private HashMap<String,RenderPipelineElement> elements=new HashMap<String,RenderPipelineElement>();
	private ArrayList<RenderPipelineElement> starting=new ArrayList<RenderPipelineElement>();
	private RenderPipelineElement ending=null;
	private String name;
	public RenderPipeline(String name) {
		this.name=name;
		colorBufferPool=new ColorBufferPool(name+"'s ColorBuffer");
	}
	public String getName() {
		return name;
	}
	public HashMap<String,RenderPipelineElement> getElements() {
		return elements;
	}
	public void add(PipelineAdditionRoutine r) {
		RenderPipelineElement e=r.run();
		e.setPipeline(this);
		elements.put(e.getName(),e);
	}
	public void setupElements() {
		//Process all hooks and timeprofiler names
		int i=0;
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().processHooks();
			if(!map.containsKey(e.getKey())) {
				map.put(e.getKey(),i);
				i++;
				e.getValue().setTimeprofilerID(i);
			} else {
				e.getValue().setTimeprofilerID(map.get(e.getKey()));
			}
			e.getValue().checkStatus();
			if(e.getValue().status==0) {
				starting.add(e.getValue());
			} else if(e.getValue().status==2) {
				if(ending!=null) {
					Logger.log(4,"There can't be two ending pipeline elements.");
				}
				ending=e.getValue();
			}
		}
		String[] timeprofilernames=new String[map.size()];
		for(Entry<String,Integer> e : map.entrySet()) {
			timeprofilernames[e.getValue()]=e.getKey();
		}
		timeprofiler=new TimeProfiler(timeprofilernames);
		if(starting.isEmpty()) {
			Logger.log(4,"No starting elements.");
		}
	}
	public FrameBuffer run() {
		timeprofiler.clear();
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().reset();
		}
		for(RenderPipelineElement e : starting) {
			e.fillBlankInputs();
			e.executeIfFilled();
		}
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().postexec();
		}
		timeprofiler.submit();
		return ending==null?null:ending.getOutputs();
	}
}
