package com.github.ranchordo.lepton.util;

import static org.lwjgl.glfw.GLFW.*;

import java.nio.DoubleBuffer;
import java.util.HashMap;

import javax.vecmath.Vector2d;

import org.lwjgl.BufferUtils;

/**
 * Quick way to handle keyboard inputs thru GLFW.
 */
public class InputHandler {
	private long win;
	private HashMap<Integer,Boolean> rise=new HashMap<Integer,Boolean>();
	public InputHandler(long win_) {
		win=win_;
	}
	public long getWin() {
		return win;
	}
	public boolean i(int key) {
		boolean ret=glfwGetKey(win,key)==GLFW_PRESS;
		return ret;
	}
	public boolean ir(int key) {
		boolean ret=i(key);
		boolean sret=false;
		if(rise.containsKey(key)) {
			if(ret && !rise.get(key)) {
				sret=true;
			}
		}
		rise.put(key,ret);
		return sret;
	}
	public boolean m(int key) {
		return glfwGetMouseButton(win,key)==GLFW_PRESS;
	}
	public boolean mr(int key) {
		boolean ret=m(key);
		boolean sret=false;
		if(rise.containsKey(key)) {
			if(ret && !rise.get(key)) {
				sret=true;
			}
		}
		rise.put(key,ret);
		return sret;
	}
	private DoubleBuffer mx=BufferUtils.createDoubleBuffer(1);
	private DoubleBuffer my=BufferUtils.createDoubleBuffer(1);
	private Vector2d mret=new Vector2d();
	/**
	 * Get mouse position
	 */
	public Vector2d mp() {
		mx.clear(); my.clear();
		glfwGetCursorPos(win,mx,my);
		mret.set(mx.get(0),my.get(0));
		return mret;
	}
}
