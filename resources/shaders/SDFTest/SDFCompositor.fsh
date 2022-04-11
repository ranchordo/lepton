#version 430 core

#define NORMSTEP 0.001
#define R_THSHLD 0.1

struct Particle {
	vec4 p;
	vec4 v;
};
layout (std140) buffer particles_buffer {
	Particle particles[];
};

layout (location = 0) out vec4 pAlbedo;
layout (location = 1) out vec4 pSpecular;
layout (location = 2) out vec4 pRoughness;
layout (location = 3) out vec4 gPosition;
layout (location = 4) out vec4 gNormal;

in vec2 texcoords;
in vec3 fragPos;

uniform vec2 dimensions;

uniform mat4 proj_matrix;
uniform mat4 world2view;
uniform int numParticles;
float distToSphere(vec3 p, vec3 origin, float r) {
	return length(p-origin)-r;
}
float smoothmin(float a, float b, float k) {
	float h=max(k-abs(a-b),0.0)/k;
	return min(a,b) - h*h*k*0.25;
}
float distToScene(vec3 p) {
	float d=1000000.0;
	for(int i=0;i<numParticles;i++) {
		d=smoothmin(d, distToSphere(p, particles[i].p.xyz, 0.5),0.5);
	}
	return d;
}

vec3 estimateNormal(vec3 p) {
	float o=distToScene(p);
	return normalize(vec3(
			distToScene(p+vec3(NORMSTEP,0,0))-o,
			distToScene(p+vec3(0,NORMSTEP,0))-o,
			distToScene(p+vec3(0,0,NORMSTEP))-o));
}
void main() {
	vec3 dir=normalize(inverse(mat3(world2view))*fragPos);
	vec3 p=(inverse(world2view)[3]).xyz;
	float d=0;
	float td=0;
	for(int i=0;i<30;i++) {
		d=distToScene(p);
		p+=d*dir;
		td+=d;
	}
	td=(d<R_THSHLD)?td:0;
	bool hit=d<R_THSHLD;
	vec3 wpos=(normalize(inverse(mat3(world2view))*fragPos)*td)+(inverse(world2view)[3]).xyz;
	pAlbedo=int(hit)*vec4(1,1-max(min(wpos.y/6,1),0),1,1);
	pSpecular=int(hit)*vec4(0.02,0,1,1);
	pRoughness=int(hit)*vec4(2.0,0,td,1);
	gPosition=int(hit)*vec4(wpos,1);
	gNormal=int(hit)*vec4(estimateNormal(wpos),1);
}