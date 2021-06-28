package lepton.engine.audio;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import lepton.util.LeptonUtil;

import static org.lwjgl.openal.AL10.*;

/**
 * Wrapper for sample buffer.
 */
public class Sound {
	private Integer bufferId;
	public Sound() {}
	public Sound(String fname) {getFile(fname);}
	public int buffer() {return bufferId;}
	private IntBuffer delbuf=BufferUtils.createIntBuffer(1);
	private int[] delbuf_arr=new int[1];
	public void getFile(String fname) {
		if(bufferId!=null) {
			free();
		}
		bufferId=Audio.getOGG(fname);
	}
	public void free() {
		delbuf_arr[0]=bufferId;
		LeptonUtil.asIntBuffer(delbuf_arr,delbuf);
		alDeleteBuffers(delbuf);
	}
}
