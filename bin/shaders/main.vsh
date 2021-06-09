#version 330 core
#define pi 3.1415926535897932384
#define MAX_LIGHTS 50

uniform int types[MAX_LIGHTS]=int[MAX_LIGHTS](0);
uniform vec3 prop[MAX_LIGHTS]=vec3[MAX_LIGHTS](vec3(0,0,0));
uniform vec4 intensities[MAX_LIGHTS]=vec4[MAX_LIGHTS](vec4(0,0,0,1));
uniform mat4 master_matrix=mat4(1.0);
uniform mat4 world2view=mat4(1.0);
uniform mat4 proj_matrix=mat4(1.0);
uniform float useLighting=2;

attribute vec3 glv;
attribute vec3 gln;
attribute vec4 glc;
attribute vec2 mtc0;
attribute vec3 tangent;
attribute vec3 bitangent;
attribute vec4 material;

varying vec4 intensity;
varying vec2 texCoords;
varying vec4 col;
varying vec3 world_position;
varying vec3 view_position;
varying vec3 normal_orig;
varying mat3 TBN;
varying vec4 material_v;
varying vec3 campos;
void main() {
	material_v=material;
	if(material.y<=0) {
		material_v.y=1.0f;
	}
	float altValue=0.15;
	campos=(inverse(world2view)[3]).xyz;
	mat4 mvp=proj_matrix*world2view*master_matrix;
	gl_Position=proj_matrix*world2view*vec4(1.0*((master_matrix*vec4(glv,1.0)).xyz),1.0);
	texCoords=mtc0.st;
	mat3 view_matrix=mat3(master_matrix);
	normal_orig=normalize(view_matrix*gln);
	vec3 tan_world=normalize(view_matrix*tangent);
	vec3 bit_world=normalize(view_matrix*bitangent);
	TBN=mat3(tan_world,bit_world,normal_orig);
	intensity=vec4(0,0,0,1);
	world_position=1.0*((master_matrix*vec4(glv,1.0)).xyz);
	view_position=(world2view*vec4(1.0*((master_matrix*vec4(glv,1.0)).xyz),1.0)).xyz;
	if(useLighting<1) {
		intensity=vec4(altValue,altValue,altValue,1);
	}
	col=glc;//vec4(((tangent/2.0)+0.5)*0.15,1);
}