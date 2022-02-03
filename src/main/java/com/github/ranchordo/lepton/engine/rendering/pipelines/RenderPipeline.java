package com.github.ranchordo.lepton.engine.rendering.pipelines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.github.ranchordo.lepton.engine.rendering.FrameBuffer;
import com.github.ranchordo.lepton.util.TimeProfiler;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

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
	protected RenderPipelineElement startingElement=null;
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
		invalid=true;
	}
	protected boolean invalid=false;
	private ArrayList<RenderPipelineElement> execOrder=new ArrayList<RenderPipelineElement>();
	public void setupElements() {
		//Process all hooks and timeprofiler names//
		int i=0;
		HashMap<String,Integer> map=new HashMap<String,Integer>();
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().processAllHooks();
			if(!map.containsKey(e.getKey())) {
				map.put(e.getKey(),i);
				i++;
				e.getValue().setTimeprofilerID(i);
			} else {
				e.getValue().setTimeprofilerID(map.get(e.getKey()));
			}
		}
		String[] timeprofilernames=new String[map.size()];
		for(Entry<String,Integer> e : map.entrySet()) {
			timeprofilernames[e.getValue()]=e.getKey();
		}
		timeprofiler=new TimeProfiler(timeprofilernames);
		
		//Generate buffer tree and execution order
		if(startingElement==null) {
			Logger.log(4,name+": No starting element.");
		}
		generateBufferTree(startingElement,execOrder,null);
	}
	//Recursive function behind generating the buffer tree and execution order
	private void generateBufferTree(RenderPipelineElement element, ArrayList<RenderPipelineElement> execOrder, FrameBuffer buffer) {
		if(buffer==null) {buffer=element.generateStartingFramebuffer();}
		element.setBuffer(buffer);
		execOrder.add(element);
		switch(element.getOutHooks().size()) {
		case 0:
			//Do nothing
			break;
		case 1:
			generateBufferTree(element.getOutHooks().get(0).element,execOrder,buffer);
			break;
		default:
			Logger.log(4,"NOOOOOO");
		}
	}
	public void run() {
		timeprofiler.clear();
		for(RenderPipelineElement e : execOrder) {
			e.execute();
		}
		timeprofiler.submit();
	}
}
