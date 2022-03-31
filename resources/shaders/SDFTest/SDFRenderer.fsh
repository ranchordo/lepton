#version 430 core

layout (location = 0) out vec4 pAlbedo;
layout (location = 1) out vec4 pSpecular;
layout (location = 2) out vec4 pRoughness;
layout (location = 3) out vec4 gPosition;
layout (location = 4) out vec4 gNormal;

in vec2 texcoords;
in vec3 fragPos;
uniform sampler2D screen;
uniform vec2 dimensions;
vec4 tangentVector(vec2 tc, vec2 dtc) {
	vec3 t1=texture(screen,tc).xyz;
	vec3 t2=texture(screen,tc+dtc).xyz;
	vec3 fp1=vec3(t1.yz,fragPos.z);
	vec3 fp2=vec3(t2.yz,fragPos.z);
	float nhit=min(1,int(t1.x>(1<<15))+int(t2.x>(1<<15)));
	return vec4(nhit,normalize((t2.x*normalize(fp2))-(t1.x*normalize(fp1))));
}
vec3 someTangentVector(vec2 tc, vec2 dtc) {
	vec4 ret=tangentVector(tc,dtc);
	if(ret.x<0.5) {return ret.yzw;}
	ret=-tangentVector(tc,-dtc);
	if(ret.x<0.5) {return ret.yzw;}
	ret=vec4(0,normalize(dtc),0);
	return ret.yzw;
}
void main() {
	vec2 pixels=1/dimensions;
	vec3 tv1=someTangentVector(texcoords,pixels*vec2(1,0));
	vec3 tv2=someTangentVector(texcoords,pixels*vec2(0,1));
	vec3 normal=normalize(cross(tv1,tv2));
	float dist=texture(screen,texcoords).x;
	float hit=int(dist<(1<<15));
	pAlbedo=hit*vec4(0,0,1,1);
	pSpecular=hit*vec4(0.02,0,1,1);
	pRoughness=hit*vec4(2.0,0,dist,1);
	gPosition=hit*vec4(0,0,0,1);
	gNormal=hit*vec4(normal,1);
}