package lepton.engine.graphics2d.tiles;

import java.util.ArrayList;

import javax.vecmath.Vector4f;

public class Group2d extends Tile2d {
	private ArrayList<Tile2d> things=new ArrayList<Tile2d>();
	@Override public void init() {
		for(Tile2d thing : things) {
			thing.init();
		}
	}
	@Override public void render() {
		for(Tile2d thing : things) {
			thing.render();
		}
	}
	@Override public void logic() {
		for(Tile2d thing : things) {
			thing.logic();
		}
	}
	private Vector4f bb=new Vector4f(0,0,0,0);
	public Tile2d add(Tile2d i) {
		things.add(i);
		return i;
	}
	public ArrayList<Tile2d> getList() {
		return things;
	}
	@Override public Tile2d setParent(Tile2d parent) {
		for(Tile2d thing : things) {
			thing.setParent(parent);
		}
		this.parent=parent;
		return this;
	}
	@Override
	public Vector4f getBoundingBox() {
		bb.set(0,0,0,0);
		for(Tile2d i : things) {
			Vector4f ibb=i.getBoundingBox();
			if(bb.x==0&&bb.y==0&&bb.z==0&&bb.w==0) {
				bb.set(ibb);
			} else {
				if(ibb.x<bb.x) {
					bb.x=ibb.x;
				}
				if(ibb.y<bb.y) {
					bb.y=ibb.y;
				}
				if(ibb.z>bb.z) {
					bb.z=ibb.z;
				}
				if(ibb.w>bb.w) {
					bb.w=ibb.w;
				}
			}
		}
		return bb;
	}
}
