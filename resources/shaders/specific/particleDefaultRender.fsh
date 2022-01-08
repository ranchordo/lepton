#version 430 core
<<<<<<< Updated upstream

#define FALLOFF 0.01
layout (location = 0) out vec4 FragColor;

in vec2 texcoords;
in int instanceID;
=======
#define FALLOFF 0.1
layout (location = 0) out vec4 FragColor;

varying vec2 texcoords;
varying int instanceID;
uniform int num_lights;
varying vec4 world_position;

struct Light {
	float type;
	vec3 prop;
	vec4 l_intensity;
};

layout (std140) buffer lights_buffer {
	Light lights_arr[];
};
>>>>>>> Stashed changes

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