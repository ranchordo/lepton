#version 430 core

layout (std140) buffer particles_buffer {
	vec4 particles[];
};


attribute vec3 glv;
attribute vec2 mtc0;

varying vec2 texcoords;
varying int instanceID;
uniform mat4 proj_matrix;
uniform mat4 world2view;

void main() {
	instanceID=int(gl_InstanceID);
	vec4 particle=particles[instanceID];
	vec3 campos=(inverse(world2view)[3]).xyz;
	vec3 viewdir=normalize(campos-particle.xyz);
	vec3 v2=normalize(cross(vec3(0,1,0),viewdir));
	vec3 v1=normalize(cross(v2,viewdir));
	mat3 rot=mat3(v1,v2,viewdir);
	gl_Position=proj_matrix*world2view*vec4(((rot*glv.xyz)*1.5)+particle.xyz,1.0);
	texcoords=mtc0;
}