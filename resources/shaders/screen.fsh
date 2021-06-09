#version 330 core

layout (location = 0) out vec4 FragColor;

varying vec2 texcoords;

uniform float exposure;
uniform float gamma;
uniform sampler2D screen;
uniform sampler2D bloom;
uniform sampler2D ssao;
uniform sampler2D ssaoMul;
void main() {
	vec3 hdr=texture2D(screen,texcoords).xyz;
	vec3 bl=texture2D(bloom,texcoords).xyz;
	vec3 ssao=texture2D(ssao,texcoords).xyz;
	vec3 ssaoMul=texture2D(ssaoMul,texcoords).xyz;
	//hdr-=ssaoMul*ssao*5.0;
	hdr+=bl;
	vec3 mapped=vec3(1.0)-exp(-hdr*exposure);
	mapped=pow(mapped,vec3(1.0/gamma));
	FragColor=vec4(mapped,1);
}	