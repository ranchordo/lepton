#version 330 core

in vec3 glv;
in vec2 mtc0;

out vec2 texcoords;
out vec3 campos;
uniform mat4 proj_matrix;
uniform mat4 world2view;
void main() {
	gl_Position=proj_matrix*vec4(glv,1.0);
	campos=(inverse(world2view)[3]).xyz;
	texcoords=mtc0;
}