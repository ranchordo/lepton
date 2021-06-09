package org.lepton.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.lepton.engine.rendering.Tri;
import org.lepton.engine.rendering.VertexMap;
import org.lwjgl.BufferUtils;

/**
 * Dumb. Do not use.
 */
public class OBJReturnObject {
	public ByteBuffer indices;
	public ByteBuffer vertices;
	public int numTriangles;
	public void fromVMap(VertexMap vmap, ArrayList<Tri> tris) {
		this.numTriangles=tris.size();
		this.indices=BufferUtils.createByteBuffer(tris.size()*3*4).order(ByteOrder.nativeOrder());
		for(int i=0;i<tris.size();i++) {
			this.indices.putInt((int) tris.get(i).vertices[0]);
			this.indices.putInt((int) tris.get(i).vertices[1]);
			this.indices.putInt((int) tris.get(i).vertices[2]);
		}
		this.vertices=BufferUtils.createByteBuffer(vmap.vertices.size()*3*4).order(ByteOrder.nativeOrder());
		for(int i=0;i<vmap.vertices.size();i++) {
			this.vertices.putFloat(vmap.vertices.get(i).x);
			this.vertices.putFloat(vmap.vertices.get(i).y);
			this.vertices.putFloat(vmap.vertices.get(i).z);
		}
		this.indices.rewind();
		this.vertices.rewind();
	}
}
