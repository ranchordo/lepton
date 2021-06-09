#version 330 core

#define pi 3.1415926535897932384
#define MAX_LIGHTS 50

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 gPosition;
layout (location = 2) out vec4 gNormal;
//layout (location = 3) out vec4 gAOMul;

uniform int types[MAX_LIGHTS]=int[MAX_LIGHTS](0);
uniform vec3 prop[MAX_LIGHTS]=vec3[MAX_LIGHTS](vec3(0,0,0));
uniform vec4 intensities[MAX_LIGHTS]=vec4[MAX_LIGHTS](vec4(0,0,0,1));
uniform float useLighting=2;
uniform mat4 world2view=mat4(1.0);

uniform float useDetail=0;

varying vec2 texCoords;
varying vec4 intensity;
varying vec4 col;
varying vec3 world_position;
varying vec3 view_position;
varying vec3 normal_orig;
varying mat3 TBN;
varying vec4 material_v;
varying vec3 campos;
uniform sampler2D tex;
uniform sampler2D bump;
uniform sampler2D norm;
uniform float useTextures=0;
void main() {
	vec4 intensity_in=intensity;
	vec3 normal=normalize(vec3(normal_orig.x,normal_orig.y,normal_orig.z));
	if(useDetail>=1) {
		vec3 normal_tex=texture2D(norm,texCoords).xyz;
		normal_tex=normal_tex*2.0 - 1.0;
		normal_tex=vec3(-normal_tex.x,normal_tex.y,normal_tex.z);
		normal=normalize(TBN * normal_tex);
		//normal=TBN * normal_tex;
		//normal=normal+1.0f;
	}
	vec3 ambient=vec3(0,0,0);
	if(useLighting>=1) {
		for(int i=0;i<MAX_LIGHTS;i++) {
			if(types[i]==0) {
				break;
			} else if(types[i]==1) {
				//Ambient light
				intensity_in=intensity_in+vec4(intensities[i].xyz,0);
				ambient+=intensities[i].xyz;
			} else if(types[i]==2) {
				//Directional light
				vec3 lightVector=normalize(prop[i]);
				float dot_prod=max(0.0,dot(normal,lightVector));
				vec3 viewdir=normalize(campos-world_position);
				//vec3 reflectdir=reflect(-lightVector,normal);
				vec3 halfdir=normalize(lightVector+viewdir);
				float spec = pow(max(dot(normal,halfdir), 0.0), material_v.y*4.0);
				intensity_in=intensity_in+vec4(intensities[i].xyz*dot_prod,0);
				intensity_in=intensity_in+vec4(intensities[i].xyz*spec*material_v.x,0);
			} else if(types[i]==3) {
				//Positional light
				float r=sqrt(pow(world_position.x-prop[i].x,2)+pow(world_position.y-prop[i].y,2)+pow(world_position.z-prop[i].z,2));
				vec3 lightVector=normalize(world_position-prop[i]);
				float dotprod=max(0.0,-dot(normal,lightVector));
				intensity_in=intensity_in+vec4(intensities[i].xyz*dotprod*(1.0/pow(r,2)),0);
				
				vec3 viewdir=normalize(campos-world_position);
				//vec3 reflectdir=reflect(-lightVector,normal);
				vec3 halfdir=normalize(-lightVector+viewdir);
				float spec = pow(max(dot(normal,halfdir), 0.0), material_v.y*4.0);
				intensity_in=intensity_in+vec4(intensities[i].xyz*spec*material_v.x,0);
			}
		}
	}
	vec4 fcol=vec4((intensity_in * col).xyz,col.w);
	vec4 tcol=texture2D(tex,texCoords);
	if(useTextures>=1) {
		fcol=fcol*tcol;
	}
	
	FragColor=fcol;//vec4(((normal/2.0)+0.5)*0.15,1);
	//gPosition=vec4(view_position,fcol.w);
	//mat3 w2v_mat3=mat3(world2view);
	//gNormal=vec4(normalize(w2v_mat3*normal),fcol.w);
	//vec3 noamb=(fcol*((intensity_in-vec4(ambient,0))/intensity_in)).xyz;
	//if(useLighting<1) {
	//	noamb=fcol.xyz;
	//}
	//gAOMul=vec4(fcol.xyz-noamb,1.0);
}