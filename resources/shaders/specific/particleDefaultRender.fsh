#version 430 core

#define FALLOFF 0.01
layout (location = 0) out vec4 FragColor;

in vec2 texcoords;
in int instanceID;

void main() {
	FragColor=vec4(1,1,1,0.05*(pow(FALLOFF,length(texcoords*2-1))-FALLOFF));
}	