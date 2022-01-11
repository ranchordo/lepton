#version 430 core

struct Particle {
	vec4 p;
	vec4 v;
};
layout (std140) buffer particles_buffer {
	Particle particles[];
};

in vec3 glv;
in vec2 mtc0;

out vec2 texcoords;
out vec4 world_position;
uniform mat4 proj_matrix;
uniform mat4 world2view;

void main() {
	int instanceID=int(gl_InstanceID);
	vec4 particle=particles[instanceID].p;
	vec3 campos=(inverse(world2view)[3]).xyz;
	vec3 viewdir=normalize(campos-particle.xyz);
	vec3 v2=normalize(cross(vec3(0,1,0),viewdir));
	vec3 v1=normalize(cross(v2,viewdir));
	mat3 rot=mat3(v1,v2,viewdir);
	world_position=vec4((rot*(vec3(glv.xy*2-1,glv.z)*1.2))+particle.xyz,1.0);
	gl_Position=proj_matrix*world2view*world_position;
	texcoords=mtc0;
}
