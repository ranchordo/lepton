#version 330 core

layout (location = 0) out vec4 FragColor0;
layout (location = 1) out vec4 FragColor1;
layout (location = 2) out vec4 FragColor2;
layout (location = 3) out vec4 FragColor3;
layout (location = 4) out vec4 FragColor4;
layout (location = 5) out vec4 FragColor5;

in vec2 texcoords;

uniform sampler2D screen;
void main() {
	FragColor0=vec4(0,0,0,1);
	FragColor1=vec4(0,0,0,1);
	FragColor2=vec4(0,0,0,1);
	FragColor3=vec4(0,0,0,1);
	FragColor4=vec4(0,0,0,1);
	FragColor5=vec4(0,0,0,1);
}	