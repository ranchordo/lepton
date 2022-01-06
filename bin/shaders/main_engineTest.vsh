#version 330 core
#define pi 3.1415926535897932384
#define MAX_LIGHTS 50

uniform mat4 master_matrix=mat4(1.0);
uniform mat4 world2view=mat4(1.0);
uniform mat4 proj_matrix=mat4(1.0);
uniform float useLighting=2;

in vec3 glv;
in vec3 gln;
in vec4 glc;
in vec2 mtc0;
in vec3 tangent;
in vec3 bitangent;
in vec4 material;

out vec4 intensity;
out vec2 texCoords;
out vec4 col;
out vec3 world_position;
out vec3 view_position;
out vec3 normal_orig;
out mat3 TBN;
out vec4 material_v;
out vec3 campos;
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