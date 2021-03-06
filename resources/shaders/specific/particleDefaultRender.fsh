#version 430 core
#define FALLOFF 0.5
layout (location = 0) out vec4 FragColor;

struct Light {
	float type;
	vec3 prop;
	vec4 l_intensity;
};

layout (std140) buffer lights_buffer {
	Light lights_arr[];
};

in vec2 texcoords;
in vec4 world_position;

uniform int num_lights;

void main() {
	vec4 intensity_in=vec4(0,0,0,0);
	for(int i=0;i<num_lights;i++) {
		Light light=lights_arr[i];
		if(round(light.type)==1) {
			//Ambient light
			intensity_in.xyz+=light.l_intensity.xyz;
		} else if(round(light.type)==2) {
			//Directional light
			intensity_in.xyz+=light.l_intensity.xyz;
		} else if(round(light.type)==3) {
			//Positional light
			float r=length(world_position.xyz-light.prop);
			intensity_in=intensity_in+vec4(light.l_intensity.xyz*(1.0/pow(r,2)),0);
		}
	}
	FragColor=vec4(intensity_in.xyz,max(0,0.5*(pow(FALLOFF,length(texcoords*2-1))-FALLOFF)));
}	
