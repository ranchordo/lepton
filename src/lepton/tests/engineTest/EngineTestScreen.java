package lepton.tests.engineTest;

import static lepton.engine.rendering.GLContextInitializer.fr;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import lepton.engine.graphics2d.tiles.Group2d;
import lepton.engine.graphics2d.tiles.PieChart;
import lepton.engine.graphics2d.tiles.TextGroup;
import lepton.engine.graphics2d.tiles.Tile2d;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.instanced.InstancedRenderer;
import lepton.util.LeptonUtil;

public class EngineTestScreen extends Tile2d {
	public Group2d group=new Group2d();
	@Override
	public void init() {
		Tile2d t=group.add(new TextGroup("No data",EngineTest.fonts.get("consolas"),-1,0.9f,0.05f,0.2f,0.2f,0.2f,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.TOP_LEFT).initThis());
		group.add(new PieChart(EngineTest.timeProfiler.times.length,0.9f,-0.9f,0.4f,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.BOTTOM_RIGHT).initThis());
		t.mouseOn=new Tile2d.EventListener() {@Override public void onEvent() {System.out.println("dbgtext.event.mouseOn");}};
		t.mouseOff=new Tile2d.EventListener() {@Override public void onEvent() {System.out.println("dbgtext.event.mouseOff");}};
		t.mouseClick=new Tile2d.EventListener() {@Override public void onEvent() {System.out.println("dbgtext.event.mouseClick");}};
		t.mouseClickRight=new Tile2d.EventListener() {@Override public void onEvent() {System.out.println("dbgtext.event.mouseClickRight");}};
		t.mouseClickMiddle=new Tile2d.EventListener() {@Override public void onEvent() {System.out.println("dbgtext.event.mouseClickMiddle");}};
		group.add(new TextGroup("No data",EngineTest.fonts.get("consolas"),-1,0.8f,0.03f,0.2f,0.2f,0.2f,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.TOP_LEFT).initThis());
		group.add(new TextGroup("No data",EngineTest.fonts.get("consolas"),-1,0.74f,0.03f,0.2f,0.2f,0.2f,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.TOP_LEFT).initThis());
		group.add(new TextGroup("Use arrow keys and SPACE to move",EngineTest.fonts.get("consolas"),-1,0.64f,0.04f,0.2f,0.2f,0.2f,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.TOP_LEFT).initThis());
		float height=-0.9f;
		for(int i=EngineTest.timeProfiler.times.length-1;i>=0;i--) {
			Vector3f col=((PieChart)group.getList().get(1)).rgbs[i];
			height+=0.05;
			group.add(new TextGroup(EngineTest.timeProfiler.time_names[i],EngineTest.fonts.get("consolas"),0.4f,height,0.03f,col.x,col.y,col.z,Tile2d.renderer).setAsParent().setPosMode(Tile2d.PosMode.BOTTOM_RIGHT).initThis());
		}
	}
	private StringBuilder dbg=new StringBuilder();
	private int fc=0;
	@Override
	public void logic() {
		fc++;
		if(fc%20==0) {
			dbg.setLength(0);
			dbg.append("Memory usage: ");
			dbg.append(String.format("%3.2f",((float)(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(float)Runtime.getRuntime().totalMemory())*100.0f));
			dbg.append("% used of ");
			dbg.append(Runtime.getRuntime().totalMemory()/1048576.0f);
			dbg.append("MB. Frame rate (unlimited): ");
			dbg.append(String.format("%3.2f", fr));
			dbg.append("fps");
			((TextGroup)group.getList().get(0)).setString(dbg.toString());
			((TextGroup)group.getList().get(2)).setString(EngineTest.timeProfiler.toString());
			dbg.setLength(0);
			dbg.append("GPU Memory usage: ");
			int[] gmeminfo=LeptonUtil.getGPUMemoryInfo();
			if(gmeminfo[0]==0 && gmeminfo[1]==0) {
				dbg.append("failed to get GPU memory info. This probably means you're running on a different card than I expected.");
			} else {
				float perc=100.0f*(gmeminfo[0]/(float)gmeminfo[1]);
				dbg.append(String.format("%3.4f",perc));
				dbg.append("% (");
				dbg.append(gmeminfo[0]);
				dbg.append(" KB) used of ");
				dbg.append(gmeminfo[1]);
				dbg.append(" KB. If this percentage is increasing, BAD! File an issue on github.");
			}
			((TextGroup)group.getList().get(3)).setString(dbg.toString());
			PieChart pc=((PieChart)group.getList().get(1));
			for(int i=0;i<EngineTest.timeProfiler.times.length;i++) {
				pc.data[i]=(int)EngineTest.timeProfiler.times[i];
			}
		}
		group.logic();
	}
	private InstancedRenderer.InstancedRenderRoutine renderRoutine=new InstancedRenderer.InstancedRenderRoutine() {
		@Override public void run() {
			group.render();
		}
	};
	@Override
	public void render() {
		glDisable(GL_DEPTH_TEST);
		Tile2d.renderer.renderInstanced(renderRoutine);
		glEnable(GL_DEPTH_TEST);
	}
	@Override
	public Vector4f getBoundingBox() {
		return group.getBoundingBox();
	}
	@Override
	public Tile2d setParent(Tile2d parent) {
		group.setParent(parent);
		this.parent=parent;
		return this;
	}
}