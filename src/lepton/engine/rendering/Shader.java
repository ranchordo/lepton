package lepton.engine.rendering;
import static org.lwjgl.opengl.GL32.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import lepton.cpshlib.ShaderDataCompatible;
import lepton.engine.rendering.lighting.Lighting;
import lepton.engine.util.Deletable;
import lepton.util.advancedLogger.Logger;

public class Shader extends ShaderDataCompatible {
	/**
	 * A list of uniforms that are default initialized. Mostly for making sure textures bind properly.
	 */
	public static HashMap<String, Integer> defaults=new HashMap<String, Integer>();
	public static void defaultInit() {
		Texture.addRequiredUniformDefaults(defaults);
		defaults.put("screen",0);
		defaults.put("bloom",1);
		defaults.put("ssao",2);
		defaults.put("ssaoMul",3);
		
		defaults.put("iPosition",0);
		defaults.put("iNormal",1);
		defaults.put("iNoise",2);
		defaultsInited=true;
	}
	private static void printShaderError(String fname, String ext, int prog) {
		String error=glGetShaderInfoLog(prog);
		if(error.isEmpty()) {
			Logger.log(4,fname+ext+" did not compile, but the error output was blank. This typically means that your graphics driver is incorrectly configured. "
					+ "If you are using a system with two graphics cards, make sure the correct one is being used. (this is commonly caused from intel integrated)");
		}
		Logger.log(4,"In "+fname+ext+": "+error);
	}
	private static boolean defaultsInited=false;
	public static final boolean IGNORE_MISSING=true;
	private int program;
	private int vs;
	private int fs;
	private int gs=-1;
	@Override public void delete() {
		glDeleteShader(vs);
		glDeleteShader(fs);
		if(gs!=-1) {glDeleteShader(gs);}
		glDeleteProgram(program);
		rdrt();
	}
	private String fname;
	public String getFname() {return fname;}
	public HashMap<String,Integer> locationCache=new HashMap<String,Integer>();
	public Shader(String fname) {
		this.fname=fname;
		program=glCreateProgram();
		syncRequiredShaderDataValues(program, IGNORE_MISSING);
		vs=glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vs, readFile(fname+".vsh"));
		glCompileShader(vs);
		if(glGetShaderi(vs, GL_COMPILE_STATUS) != 1) {
			printShaderError(fname,".vsh",vs);
		}
		
		fs=glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, readFile(fname+".fsh"));
		glCompileShader(fs);
		if(glGetShaderi(fs, GL_COMPILE_STATUS) != 1) {
			printShaderError(fname,".fsh",fs);
		}
		
		glAttachShader(program,vs);
		glAttachShader(program,fs);
		
		String geoShader=readFile_ret(fname+".gsh");
		if(geoShader!=null) {
			Logger.log(0,"Found geometry shader "+fname+".gsh, loading it...");
			gs=glCreateShader(GL_GEOMETRY_SHADER);
			glShaderSource(gs,geoShader);
			glCompileShader(gs);
			if(glGetShaderi(gs, GL_COMPILE_STATUS) != -1) {
				printShaderError(fname,".gsh",gs);
			}
			glAttachShader(program,gs);
		}
		
		glBindAttribLocation(program,0,"glv");
		glBindAttribLocation(program,2,"gln");
		glBindAttribLocation(program,3,"glc");
		glBindAttribLocation(program,8,"mtc0");
		glBindAttribLocation(program,13,"material");
		glBindAttribLocation(program,14,"tangent");
		glBindAttribLocation(program,15,"bitangent");
		
		glLinkProgram(program);
		if(glGetProgrami(program,GL_LINK_STATUS)!=GL_TRUE) {
			Logger.log(4,"In shader "+fname+" during program linking routine: "+glGetProgramInfoLog(program));
		}
		glValidateProgram(program);
		if(glGetProgrami(program,GL_VALIDATE_STATUS)!=GL_TRUE) {
			Logger.log(4,"In shader "+fname+" during program validation routine: "+glGetProgramInfoLog(program));
		}
		this.setInitialFname(fname);
		Logger.log(0,"Loaded shader \""+fname+"\" successfully.");
		adrt();
	}
	public void bind() {
		if(GLContextInitializer.activeShader==this) {
			return;
		}
		if(!defaultsInited) {defaultInit();}
		glUseProgram(program);
		GLContextInitializer.activeShader=this;
		for(Entry<String, Integer> e : Shader.defaults.entrySet()) {
			int loc=getUniformLocation(e.getKey());
			if(loc!=-1) {glUniform1i(loc,e.getValue());}
		}
		Lighting.apply();
	}
	private String readFile(String fname) {
		String ret=readFile_ret(fname);
		if(ret==null) {
			Logger.log(4,"/shaders/"+fname+" doesn't exist.");
		}
		return ret;
	}
	private String readFile_ret(String fname) {
		StringBuilder string=new StringBuilder();
		BufferedReader b;
		try {
			b=new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream("/shaders/"+fname)));
			String line;
			while((line=b.readLine())!=null) {
				string.append(line);
				string.append("\n");
			}
			b.close();
		} catch (IOException e) {
			Logger.log(4,e.toString(),e);
		} catch (NullPointerException e) {
			return null;
		}
		return string.toString();
	}
}
