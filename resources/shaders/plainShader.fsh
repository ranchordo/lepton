#version 430 core

#define pi 3.1415926535897932384

layout (location = 0) out vec4 FragColor;
in vec4 col;
void main() {
	FragColor=col;
}