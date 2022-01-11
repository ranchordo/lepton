package lepton.engine.rendering.pipelines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import lepton.engine.rendering.FrameBuffer;
import lepton.util.TimeProfiler;
import lepton.util.advancedLogger.Logger;

public class RenderPipeline {
	private TimeProfiler timeprofiler;
	public TimeProfiler getTimeProfiler() {
		return timeprofiler;
	}
	private HashMap<String,RenderPipelineElement> elements=new HashMap<String,RenderPipelineElement>();
	private ArrayList<RenderPipelineElement> starting=new ArrayList<RenderPipelineElement>();
	private RenderPipelineElement ending=null;
	private String name;
	public RenderPipeline(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public HashMap<String,RenderPipelineElement> getElements() {
		return elements;
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
		if(starting.isEmpty() || ending==null) {
			Logger.log(4,"No starting or ending elements.");
		}
	}
	public FrameBuffer run() {
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().reset();
		}
		for(RenderPipelineElement e : starting) {
			e.execute();
			e.propagate();
		}
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().postexec();
		}
		return ending.getOutputs();
	}
}
