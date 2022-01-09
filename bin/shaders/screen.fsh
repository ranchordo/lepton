#version 330 core

layout (location = 0) out vec4 FragColor;

varying vec2 texcoords;

uniform float exposure;
uniform float gamma;
uniform sampler2D screen;
uniform sampler2D bloom;
void main() {
	vec3 hdr=texture2D(screen,texcoords).xyz;
	vec3 bl=texture2D(bloom,texcoords).xyz;
	//hdr-=ssaoMul*ssao*5.0;
	hdr+=bl;
	vec3 mapped=vec3(1.0)-exp(-hdr*exposure);
	mapped=pow(mapped,vec3(1.0/gamma));
	FragColor=vec4(mapped,1);
}	