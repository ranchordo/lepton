package com.github.ranchordo.lepton.engine.graphics2d.util;

import java.util.HashMap;
import java.util.Map.Entry;

public class Fonts {
	public HashMap<String,FontAtlas> fonts=new HashMap<String,FontAtlas>();
	public FontAtlas get(String s) {
		return fonts.get(s);
	}
	public void add(String s, String fname, String ext, int spacing, int vertOffset, String layout) {
		FontAtlas f=new FontAtlas(fname,ext,spacing,vertOffset,layout);
		f.load();
		fonts.put(s,f);
	}
	public void clean() {
		for(Entry<String,FontAtlas> e : fonts.entrySet()) {
			e.getValue().clean();
		}
	}
}
