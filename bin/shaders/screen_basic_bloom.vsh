#version 330 core

in vec3 glv;
in vec2 mtc0;
out vec2 texcoords;
uniform mat4 proj_matrix;
void main() {
	gl_Position=proj_matrix*vec4(glv,1.0);
	texcoords=mtc0;
}