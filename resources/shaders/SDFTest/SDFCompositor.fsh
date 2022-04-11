#version 430 core

layout (location = 0) out vec4 pAlbedo;
layout (location = 1) out vec4 pSpecular;
layout (location = 2) out vec4 pRoughness;
layout (location = 3) out vec4 gPosition;
layout (location = 4) out vec4 gNormal;

in vec2 texcoords;
in vec3 fragPos;

uniform vec2 dimensions;

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;
vec4 addPixel(vec2 tc, vec2 dtc) {
	vec3 normal=texture(sampler4,tc+dtc).xyz;
	return vec4(normal,int(length(normal)>1e-6));
}
void main() {
	vec2 pixels=1/dimensions;
	vec4 ret=vec4(0,0,0,0);
	ret+=addPixel(texcoords,pixels*vec2(+0,+0));
	
	ret+=addPixel(texcoords,pixels*vec2(+0,+1));
	ret+=addPixel(texcoords,pixels*vec2(+0,-1));
	ret+=addPixel(texcoords,pixels*vec2(+1,+0));
	ret+=addPixel(texcoords,pixels*vec2(-1,+0));
	
	ret+=addPixel(texcoords,pixels*vec2(+1,+1));
	ret+=addPixel(texcoords,pixels*vec2(+1,-1));
	ret+=addPixel(texcoords,pixels*vec2(-1,+1));
	ret+=addPixel(texcoords,pixels*vec2(-1,-1));
	
	ret/=ret.w;
	FragColor=vec4(ret.xyz,1);
}