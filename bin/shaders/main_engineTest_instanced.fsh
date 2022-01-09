#version 430 core

#define pi 3.1415926535897932384

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 gPosition;
layout (location = 2) out vec4 gNormal;
//layout (location = 3) out vec4 gAOMul;


struct Light {
	float type;
	vec3 prop;
	vec4 l_intensity;
};

layout (std140) buffer lights_buffer {
	Light lights_arr[];
};

uniform int num_lights=0;
uniform float useLighting=2;
uniform mat4 world2view=mat4(1.0);
uniform int textureUse=0;

varying vec2 texCoords;
varying vec4 intensity;
varying vec4 col;
varying vec3 world_position;
varying vec3 view_position;
varying vec3 normal_orig;
varying mat3 TBN;
varying vec4 material_v;
varying vec3 campos;
uniform sampler2D albedo;
uniform sampler2D normal;
void main() {
	vec4 intensity_in=intensity;
	vec3 norm=normalize(vec3(normal_orig.x,normal_orig.y,normal_orig.z));
	if((textureUse&2)>0) {
		vec3 normal_tex=texture2D(normal,texCoords).xyz;
		normal_tex=normal_tex*2.0 - 1.0;
		normal_tex=vec3(-normal_tex.x,normal_tex.y,normal_tex.z);
		norm=normalize(TBN * normal_tex);
		//normal=TBN * normal_tex;
		//normal=normal+1.0f;
	}
	vec3 ambient=vec3(0,0,0);
	if(useLighting>=1) {
		for(int i=0;i<num_lights;i++) {
			Light light=lights_arr[i];
			if(round(light.type)==1) {
				//Ambient light
				intensity_in=intensity_in+vec4(light.l_intensity.xyz,0);
				ambient+=light.l_intensity.xyz;
			} else if(round(light.type)==2) {
				//Directional light
				vec3 lightVector=normalize(light.prop);
				float dot_prod=max(0.0,dot(norm,lightVector));
				vec3 viewdir=normalize(campos-world_position);
				//vec3 reflectdir=reflect(-lightVector,norm);
				vec3 halfdir=normalize(lightVector+viewdir);
				float spec = pow(max(dot(norm,halfdir), 0.0), material_v.y*4.0);
				intensity_in=intensity_in+vec4(light.l_intensity.xyz*dot_prod,0);
				intensity_in=intensity_in+vec4(light.l_intensity.xyz*spec*material_v.x,0);
			} else if(round(light.type)==3) {
				//Positional light
				float r=sqrt(pow(world_position.x-light.prop.x,2)+pow(world_position.y-light.prop.y,2)+pow(world_position.z-light.prop.z,2));
				vec3 lightVector=normalize(world_position-light.prop);
				float dotprod=max(0.0,-dot(norm,lightVector));
				intensity_in=intensity_in+vec4(light.l_intensity.xyz*dotprod*(1.0/pow(r,2)),0);
				
				vec3 viewdir=normalize(campos-world_position);
				//vec3 reflectdir=reflect(-lightVector,norm);
				vec3 halfdir=normalize(-lightVector+viewdir);
				float spec = pow(max(dot(norm,halfdir), 0.0), material_v.y*4.0);
				intensity_in=intensity_in+vec4(light.l_intensity.xyz*spec*material_v.x,0);
			}
		}
	}
	vec4 fcol=vec4((intensity_in * col).xyz,col.w);
	if((textureUse&1)>0) {
		vec4 tcol=texture2D(albedo,texCoords);
		fcol=fcol*tcol;
	}
	
	FragColor=fcol;//vec4(((norm/2.0)+0.5)*0.15,1);
}