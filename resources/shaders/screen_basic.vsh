#version 330 core

attribute vec3 glv;
attribute vec2 mtc0;

varying vec2 texcoords;
uniform mat4 proj_matrix;
void main() {
	gl_Position=proj_matrix*vec4(glv,1.0);
	texcoords=mtc0;
}