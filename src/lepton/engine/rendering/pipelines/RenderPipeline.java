package lepton.engine.rendering.pipelines;

import java.util.HashMap;
import java.util.Map.Entry;

public class RenderPipeline {
	private HashMap<String,RenderPipelineElement> elements=new HashMap<String,RenderPipelineElement>();
	public HashMap<String,RenderPipelineElement> getElements() {
		return elements;
	}
	public void init() {
		//Process all hooks
		for(Entry<String,RenderPipelineElement> e : elements.entrySet()) {
			e.getValue().processHooks();
		}
	}
}
