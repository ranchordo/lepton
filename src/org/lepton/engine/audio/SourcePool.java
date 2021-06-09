package org.lepton.engine.audio;

import java.util.HashMap;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.lepton.optim.objpoollib.*;

/**
 * AbstractObjectPool extension for storing and managing OpenAL sources.
 * The idea behid unifiedPos and unifiedVel is to copy the pointers to a single instance of each one to each source, that way we don't need to nmanually set the vectors every single frame. Mutability FTW!
 */
public class SourcePool extends AbstractObjectPool<Source> {
	private HashMap<String, PoolElement<Source>> playing=new HashMap<String, PoolElement<Source>>();
	private Vector3f unifiedPos=new Vector3f();
	private Vector3f unifiedVel=new Vector3f();
	public SourcePool(String type) {
		super(type+".source", null);
		PoolInitCreator<Source> p=new PoolInitCreator<Source>() {
			@Override
			public Source allocateInitValue() {
				Source a=new Source();
				a.setPosVel(SourcePool.this.unifiedPos,SourcePool.this.unifiedVel);
				return a;
			}
		};
		this.setInitCreator(p);
		this.freeThshld=30000000l;
	}
	public PoolElement<Source> getPlaying(String key) {
		return playing.get(key);
	}
	/**
	 * Gotta be done on all the frames.
	 */
	public void logic(Point3f pos) {
		freeDone();
		unifiedPos.set(pos);
		unifiedVel.set(0,0,0);
		for(int i=0;i<pool.size();i++) {
			pool.get(i).o().updatePosVel();
		}
	}
	private void freeDone() {
		for(int i=0;i<pool.size();i++) {
			PoolElement<Source> a=pool.get(i);
			if(!a.o().isPlaying()) {
				playing.remove((String)a.mdo);
				a.free();
			}
		}
	}
	public void play(Sound sound, String key) {
		PoolElement<Source> a=this.alloc();
		a.mdo=key;
		playing.put(key,a);
		a.o().play(sound);
	}
	/**
	 * Play a sound from a soundtrack object.
	 */
	public void play(String key, Soundtrack soundtrack) {
		this.play(soundtrack.get(key),key);
	}
	@Override
	public void handleDeletion(PoolElement<Source> i) {
		i.o().close();
	}
}
