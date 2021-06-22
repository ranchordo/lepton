package lepton.engine.rendering;
import static org.lwjgl.opengl.GL32.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

import lepton.cpshlib.main.ShaderDataCompatible;
import lepton.engine.rendering.lighting.Lighting;
import lepton.util.advancedLogger.Logger;

public class Shader extends ShaderDataCompatible {
	public static HashMap<String, Integer> defaults=new HashMap<String, Integer>();
	public static void defaultInit() {
		defaults.put("tex",0);
		defaults.put("bump",1);
		defaults.put("norm",2);
		defaults.put("screen",0);
		defaults.put("bloom",1);
		defaults.put("ssao",2);
		defaults.put("ssaoMul",3);
		
		defaults.put("iPosition",0);
		defaults.put("iNormal",1);
		defaults.put("iNoise",2);
		defaultsInited=true;
	}
	private static boolean defaultsInited=false;
	public static final boolean IGNORE_MISSING=true;
	private int program;
	private int vs;
	private int fs;
	private int gs;
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
			System.err.println("In "+fname+".vsh: ");
			System.err.println(glGetShaderInfoLog(vs));
			System.exit(1);
		}
		
		fs=glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fs, readFile(fname+".fsh"));
		glCompileShader(fs);
		if(glGetShaderi(fs, GL_COMPILE_STATUS) != 1) {
			System.err.println("In "+fname+".fsh: ");
			System.err.println(glGetShaderInfoLog(fs));
			System.exit(2);
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
				System.err.println("In "+fname+".gsh: ");
				System.err.println(glGetShaderInfoLog(gs));
				System.exit(3);
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
		if(glGetProgrami(program,GL_LINK_STATUS)!=1) {
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		glValidateProgram(program);
		if(glGetProgrami(program,GL_VALIDATE_STATUS)!=1) {
			System.err.println(glGetProgramInfoLog(program));
			System.exit(1);
		}
		this.setInitialFname(fname);
		Logger.log(0,"Loaded shader \""+fname+"\" successfully.");
	}
	public void bind() {
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
			e.printStackTrace();
			System.exit(1);
		} catch (NullPointerException e) {
			return null;
		}
		return string.toString();
	}
}
