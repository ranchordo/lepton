#version 430 core
#define pi 3.1415926535897932384
#define MAX_LIGHTS 50

struct Info {
	mat4 obj2world;
	vec4 scaleinfo;
	vec4 texinfo;
	mat4 viewmatrix;
};

layout (std140) buffer info_buffer {
	Info infos[];
};

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
	Info info=infos[gl_InstanceID];
	mat4 master_matrix=info.obj2world;
	mat4 view=world2view*info.viewmatrix;
	material_v=material;
	if(material.y<=0) {
		material_v.y=1.0f;
	}
	float altValue=0.15;
	campos=(inverse(view)[3]).xyz;
	mat4 mvp=proj_matrix*view*master_matrix;
	gl_Position=mvp*vec4(glv*info.scaleinfo.xyz,1.0);
	texCoords=(mtc0.st*info.texinfo.zw)+info.texinfo.xy;
	mat3 view_matrix=mat3(master_matrix);
	normal_orig=normalize(view_matrix*gln);
	vec3 tan_world=normalize(view_matrix*tangent);
	vec3 bit_world=normalize(view_matrix*bitangent);
	TBN=mat3(tan_world,bit_world,normal_orig);
	intensity=vec4(0,0,0,1);
	world_position=(master_matrix*vec4(glv*info.scaleinfo.xyz,1.0)).xyz;
	view_position=(view*vec4(1.0*((master_matrix*vec4(glv,1.0)).xyz),1.0)).xyz;
	if(useLighting<1) {
		intensity=vec4(altValue,altValue,altValue,1);
	}
	col=glc;//vec4(((tangent/2.0)+0.5)*0.15,1);
}