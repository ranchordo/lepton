#version 430 core

<<<<<<< Updated upstream
layout (std140) buffer particles_buffer {
	vec4 particles[];
};


in vec3 glv;
in vec2 mtc0;

out vec2 texcoords;
out int instanceID;
=======
struct Particle {
	vec4 p;
	vec4 v;
};
layout (std140) buffer particles_buffer {
	Particle particles[];
};


attribute vec3 glv;
attribute vec2 mtc0;

varying vec2 texcoords;
varying int instanceID;
varying vec4 world_position;
>>>>>>> Stashed changes
uniform mat4 proj_matrix;
uniform mat4 world2view;

void main() {
	instanceID=int(gl_InstanceID);
<<<<<<< Updated upstream
	vec4 particle=particles[instanceID];
=======
	vec4 particle=particles[instanceID].p;
>>>>>>> Stashed changes
	vec3 campos=(inverse(world2view)[3]).xyz;
	vec3 viewdir=normalize(campos-particle.xyz);
	vec3 v2=normalize(cross(vec3(0,1,0),viewdir));
	vec3 v1=normalize(cross(v2,viewdir));
	mat3 rot=mat3(v1,v2,viewdir);
<<<<<<< Updated upstream
	gl_Position=proj_matrix*world2view*vec4(((rot*glv.xyz)*1.5)+particle.xyz,1.0);
=======
	world_position=vec4((rot*(vec3(glv.xy*2-1,glv.z)*1.2))+particle.xyz,1.0);
	gl_Position=proj_matrix*world2view*world_position;
>>>>>>> Stashed changes
	texcoords=mtc0;
}