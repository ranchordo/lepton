package lepton.engine.graphics2d.tiles;

import java.awt.Color;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.Shader;
import lepton.engine.rendering.instanced.InstanceAccumulator;
import lepton.engine.rendering.instanced.InstancedRenderer;

public class PieChart extends Tile2d {
	public static Shader pieChartShader;
	public Group2d group=new Group2d();
	public float[] data=null;
	public Vector3f[] rgbs=null;
	public float width=0.4f;
	private InstanceAccumulator ia;
	public PieChart(int elements, float nx, float ny, float nw, InstancedRenderer sl) {
		if(pieChartShader==null) {
			pieChartShader=GLContextInitializer.shaderLoader.load("specific/piechart");
		}
		ia=sl.loadConfiguration(pieChartShader,null,Tile2d.genericSquare,16,"info_buffer").instanceAccumulator;
		data=new float[elements];
		rgbs=new Vector3f[elements];
		x=nx;
		y=ny;
		width=nw;
	}
	public void init() {
		boolean tog=false;
		for(int i=0;i<data.length;i++) {
			data[i]=1;
			GenericInstancedTile2d r=new GenericInstancedTile2d(ia);
//			r.renderingShader=pieChartShader;
			r.objectSize=GenericInstancedTile2d.defaultObjectSize+8;
			tog=!tog;
			int c=Color.HSBtoRGB(i*(1.0f/data.length),tog?1:0.90f,tog?0.90f:1);
			rgbs[i]=new Vector3f(((c>>0)&0xFF)/255.0f,((c>>8)&0xFF)/255.0f,((c>>16)&0xFF)/255.0f);
			r.setParent(parent);
			r.setPosMode(posMode);
			//System.out.println(rgbs[i]);
			group.add(r);
		}
		group.init();
	}
	public void logic() {
		for(Tile2d thing : group.getList()) {
			GenericInstancedTile2d gthing=(GenericInstancedTile2d)thing;
			gthing.x=x;
			gthing.y=y;
			gthing.width=width;
			gthing.height=width;
		}
		group.logic();
		runEventListeners(); //For event debugging
	}
	@Override
	public void render() {
		float total=0;
		for(float f : data) {
			total+=f;
		}
		float offset=0;
		for(int i=0;i<group.getList().size();i++) {
			GenericInstancedTile2d thing=(GenericInstancedTile2d)group.getList().get(i);
			thing.refreshDataLength();
			thing.additionalData[0]=offset/total;
			thing.additionalData[1]=data[i]/total;
			thing.additionalData[2]=0;
			thing.additionalData[3]=0;
			thing.additionalData[4]=rgbs[i].x;
			thing.additionalData[5]=rgbs[i].y;
			thing.additionalData[6]=rgbs[i].z;
			thing.additionalData[7]=0;
			thing.render();
			offset+=data[i];
		}
	}
	@Override
	public Tile2d setParent(Tile2d parent) {
		group.setParent(parent);
		this.parent=parent;
		return this;
	}

	@Override
	public Vector4f getBoundingBox() {
		return group.getList().get(0).getBoundingBox();
	}
	
}
