package com.github.ranchordo.lepton.cpshlib;

import com.github.ranchordo.lepton.engine.util.Deletable;

import static org.lwjgl.opengl.GL46.*;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class AtomicCounterBuffer extends Deletable {
	private int buf;
	public AtomicCounterBuffer(long numCounters) {
		buf=glGenBuffers();
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, buf);
		glBufferData(GL_ATOMIC_COUNTER_BUFFER, numCounters*4, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);
		adrt();
	}
	public IntBuffer map(int mode) {
		return ShaderDataCompatible.mappifyBytes(GL_ATOMIC_COUNTER_BUFFER, buf, mode).order(ByteOrder.nativeOrder()).asIntBuffer();
	}
	public void unmap() {
		ShaderDataCompatible.unMappify(GL_ATOMIC_COUNTER_BUFFER);
	}
	public void clear() {
		IntBuffer b=map(GL_WRITE_ONLY); {
			b.clear();
			for(int i=0;i<b.capacity();i++) {
				b.put(0);
			}
		} unmap();
	}
	public void bind(int binding) {
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, buf);
		glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, binding, buf);
	}
	public void delete() {
		glDeleteBuffers(buf);
		rdrt();
	}
}
