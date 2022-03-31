#version 430 core

layout (location = 0) out vec4 FragColor;

in vec2 texcoords;
in vec3 fragPos;

float distToSphere(vec3 p, vec3 origin, float r) {
	return length(p-origin)-r;
}
float distToScene(vec3 p) {
	return distToSphere(p, vec3(0,0,-2), 0.5);
}
void main() {
	vec3 dir=normalize(fragPos);
	vec3 p=vec3(0,0,0);
	float d=0;
	float td=0;
	for(int i=0;i<20;i++) {
		d=distToScene(p);
		p+=d*dir;
		td+=d;
	}
	td=(d<1e-6)?td:(1<<16 + 1);
	FragColor=vec4(td,fragPos.x,fragPos.y,1);
}