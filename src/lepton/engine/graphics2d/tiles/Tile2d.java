package lepton.engine.graphics2d.tiles;

import static org.lwjgl.glfw.GLFW.*;

import java.util.ArrayList;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import lepton.engine.graphics2d.util.InstancedRenderer2d;
import lepton.engine.rendering.GLContextInitializer;
import lepton.engine.rendering.GObject;
import lepton.engine.rendering.Tri;
import lepton.util.InputHandler;

public abstract class Tile2d {
	/**
	 * For those little 2d engine induced aneurisms. Will force all builtin tiles to render in a definitely visible location (no shader replacement, do that yourself).
	 */
	public static boolean forceDebugRenderingParams=false;
	@FunctionalInterface
	public static interface EventListener {
		public void onEvent();
	}
	public static enum PosMode {
		CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}
	public static float computeActualX(PosMode mode, float x, Vector4f bb) {
		switch(mode) {
		case CENTER:
			return x-((bb.z-bb.x)/2.0f);
		case TOP_RIGHT:
		case BOTTOM_RIGHT:
			return x-(bb.z-bb.x);
		case TOP_LEFT:
		case BOTTOM_LEFT:
		default:
			return x;
		}
	}
	public static float computeActualY(PosMode mode, float y, Vector4f bb) {
		switch(mode) {
		case CENTER:
			return y-((bb.w-bb.y)/2.0f);
		case TOP_RIGHT:
		case TOP_LEFT:
			return y-(bb.w-bb.y);
		case BOTTOM_RIGHT:
		case BOTTOM_LEFT:
		default:
			return y;
		}
	}
	public static GObject createGenericSquare() {
		GObject ret=new GObject();
		ret.vmap.vertices=new ArrayList<Vector3f>();
		ret.vmap.vertices.add(new Vector3f(0,0,0));
		ret.vmap.vertices.add(new Vector3f(1,0,0));
		ret.vmap.vertices.add(new Vector3f(1,1,0));
		ret.vmap.vertices.add(new Vector3f(0,1,0));
		ret.vmap.normals=new ArrayList<Vector3f>();
		ret.vmap.normals.add(new Vector3f(0,0,-1));
		ret.vmap.texcoords=new ArrayList<Vector2f>();
		ret.vmap.texcoords.add(new Vector2f(0,1));
		ret.vmap.texcoords.add(new Vector2f(1,1));
		ret.vmap.texcoords.add(new Vector2f(1,0));
		ret.vmap.texcoords.add(new Vector2f(0,0));
		
		ret.clearTris();
		ret.addTri(new Tri(0,1,2, 0,0,0).setTexCoords(3,2,1));
		ret.addTri(new Tri(2,3,0, 0,0,0).setTexCoords(1,0,3));
		
		ret.lock();
		
		ret.initVBO();
		ret.refresh();
		return ret;
	}
	public static float ratio2viewportX(float x) {
		return (float) (2.0f*x*(GLContextInitializer.aspectRatio)*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
	}
	public static float ratio2viewportY(float y) {
		return (float) (2.0f*y*Math.tan(Math.toRadians(GLContextInitializer.fov/2.0f)));
	}
	public static InstancedRenderer2d renderer=new InstancedRenderer2d();
	public float x;
	public float y;
	public PosMode posMode=PosMode.CENTER;
	public Tile2d parent;
	public static GObject genericSquare=createGenericSquare();
	public GObject shape=genericSquare;
	public void init() {
		
	}
//	public void add() {
//		
//	}
	public void logic() {
		
	}
	public void render() {
		
	}
	public EventListener mouseOn=null;
	public EventListener mouseOff=null;
	public EventListener mouseClick=null;
	public EventListener mouseClickRight=null;
	public EventListener mouseClickMiddle=null;
	private byte isMouseOn=-1;
	public byte isMouseOn() {
		return isMouseOn;
	}
	private boolean prevMouseSetting=false;
	private InputHandler in=null;
	private void runEventListener(EventListener el) {
		if(el!=null) {el.onEvent();}
	}
	public void runEventListeners() {
		if(in==null) {
			in=new InputHandler(GLContextInitializer.win);
		}
		if(mouseOn!=null || mouseOff!=null || mouseClick!=null || mouseClickRight!=null || mouseClickMiddle!=null) {
			if(glfwGetInputMode(GLContextInitializer.win,GLFW_CURSOR)==GLFW_CURSOR_NORMAL) {
				Vector2d mp=in.mp();
				Vector4f bb=getBoundingBox();
				mp.x=2*(mp.x/GLContextInitializer.winW)-1;
				mp.y=2*(1-(mp.y/GLContextInitializer.winH))-1;
				if(((mp.x>=bb.x && mp.x<=bb.z)||(mp.x<=bb.x && mp.x>=bb.z)) && ((mp.y>=bb.y && mp.y<=bb.w)||(mp.y<=bb.y && mp.y>=bb.w))) {
					if(!prevMouseSetting) {
						runEventListener(mouseOn);
						isMouseOn=1;
						prevMouseSetting=true;
					}
					if(in.mr(GLFW_MOUSE_BUTTON_LEFT)) {
						runEventListener(mouseClick);
					}
					if(in.mr(GLFW_MOUSE_BUTTON_RIGHT)) {
						runEventListener(mouseClickRight);
					}
					if(in.mr(GLFW_MOUSE_BUTTON_MIDDLE)) {
						runEventListener(mouseClickMiddle);
					}
				} else {
					if(prevMouseSetting) {
						runEventListener(mouseOff);
						isMouseOn=0;
						prevMouseSetting=false;
					}
				}
			}
			
		}
	}
	public final Tile2d initThis() {
		this.init();
		return this;
	}
	public abstract Tile2d setParent(Tile2d parent);
	public Tile2d setPosMode(PosMode npm) {
		this.posMode=npm;
		return this;
	}
	public Tile2d setAsParent() {
		this.setParent(this);
		return this;
	}
	public abstract Vector4f getBoundingBox();
}
