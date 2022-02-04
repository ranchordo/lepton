#version 330 core

layout (location = 0) out vec4 FragColor;

in vec2 texcoords;

uniform sampler2D screen;
void main() {
	vec3 hdr=texture(screen,texcoords).xyz;
	FragColor=vec4(hdr,1);
}	