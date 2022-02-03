package com.github.ranchordo.lepton.engine.animation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.github.ranchordo.lepton.util.LeptonUtil;
import com.github.ranchordo.lepton.util.advancedLogger.Logger;

/**
 * Parse out an animation from an ani file. I can get you instructions on how to generate these from blender using a python script.
 */
public class AnimParser {
	/**
	 * Requires no file extension.
	 */
	public static AnimTrack parse(String fname) {
		try {
			return parse(new FileInputStream(LeptonUtil.getExternalPath()+"/"+fname+".ani"));
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
			System.exit(1);
		}
		return null;
	}
	public static AnimTrack parse(InputStream f) throws IOException {
		AnimTrack ret=new AnimTrack();
		BufferedReader reader=new BufferedReader(new InputStreamReader(f));
		String line;
		Vector3f loc=new Vector3f();
		Quat4f quat=new Quat4f();
		float sc;
		int frame;
		while((line=reader.readLine())!=null) {
			if(line.startsWith("f ")) {
				String[] c=line.split(" ");
				loc.set(Float.parseFloat(c[2]),Float.parseFloat(c[3]),Float.parseFloat(c[4]));
				quat.set(Float.parseFloat(c[5]),Float.parseFloat(c[6]),Float.parseFloat(c[7]),Float.parseFloat(c[8]));
				sc=Float.parseFloat(c[9]);
				frame=Integer.parseInt(c[1]);
				ret.keyframes.add(new Keyframe(loc,quat,sc,frame));
			} else if(line.startsWith("fps ")) {
				String[] c=line.split(" ");
				ret.setPlaybackFPS(Float.parseFloat(c[1]));
			}
		}
		return ret;
	}
}
