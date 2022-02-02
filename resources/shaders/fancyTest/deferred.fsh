#version 430 core

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BloomColor;

in vec2 texcoords;
in vec3 campos;

struct Light {
	float type;
	vec3 prop;
	vec4 l_intensity;
};

layout (std140) buffer lights_buffer {
	Light lights_arr[];
};
uniform int num_lights=0;
uniform mat4 world2view;
uniform vec2 windowpixels=vec2(1,1);
uniform float bloom_thshld=1.0;
uniform float fog_k=0.1;
uniform float fog_d=25;
uniform float useFog=0;
uniform vec4 fog_color=vec4(1,0,1,1);
uniform vec4 altLightingValue=vec4(1,1,1,1);

uniform sampler2DMS sampler0;
uniform sampler2DMS sampler1;
uniform sampler2DMS sampler2;
uniform sampler2DMS sampler3;
uniform sampler2DMS sampler4;
void main() {
	//FragColor=texelFetch(sampler4,texcoords);
	vec4 material_v=vec4(0.01,32,0,0);
	vec4 intensity_in=vec4(0,0,0,1);
	ivec2 tcoords=ivec2(windowpixels*texcoords);
	vec4 alb=texelFetch(sampler0,tcoords,gl_SampleID);
	vec4 spec=texelFetch(sampler1,tcoords,gl_SampleID);
	vec4 rough=texelFetch(sampler2,tcoords,gl_SampleID);
	vec3 world_position=texelFetch(sampler3,tcoords,gl_SampleID).xyz;
	vec3 norm=texelFetch(sampler4,tcoords,gl_SampleID).xyz;
	for(int i=0;i<num_lights;i++) {
		Light light=lights_arr[i];
		if(round(light.type)==1) {
			//Ambient light
			intensity_in=intensity_in+vec4(light.l_intensity.xyz,0);
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
	bool useLighting=(spec.z>=0.5);
	intensity_in=int(useLighting)*intensity_in+int(!useLighting)*altLightingValue;
	float depth=(1.0/(1+exp(fog_k*(max(-rough.z,0)-fog_d))))*int(abs(rough.z)>0.001);
	depth=int(useFog>=1)*(depth-1) + 1;
	depth=min(max(0,depth),1);
	FragColor=intensity_in*alb*depth+vec4(fog_color.xyz*(1-depth),1.0);
	float brightness=dot(FragColor.xyz, vec3(0.2126, 0.7152, 0.0722));
	BloomColor=vec4(FragColor.xyz*int(brightness>bloom_thshld),1);
}	