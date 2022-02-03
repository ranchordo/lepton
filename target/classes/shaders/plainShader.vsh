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

out vec4 col;
void main() {
	mat4 mvp=proj_matrix*world2view*master_matrix;
	gl_Position=proj_matrix*world2view*vec4(1.0*((master_matrix*vec4(glv,1.0)).xyz),1.0);
	col=glc;//vec4(((tangent/2.0)+0.5)*0.15,1);
}