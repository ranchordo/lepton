#version 430 core

#define pi 3.1415926535897932384

layout (location = 0) out vec4 pAlbedo;
layout (location = 1) out vec4 pSpecular;
layout (location = 2) out vec4 pRoughness;
layout (location = 3) out vec4 gPosition;
layout (location = 4) out vec4 gNormal;

struct Light {
	float type;
	vec3 prop;
	vec4 l_intensity;
};

layout (std140) buffer lights_buffer {
	Light lights_arr[];
};

uniform int num_lights=0;
uniform float useLighting=2;
uniform float useSSR=0;
uniform mat4 world2view=mat4(1.0);
uniform int textureUse=0;

in vec2 texCoords;
in vec4 col;
in vec3 world_position;
in vec3 view_position;
in vec3 normal_orig;
in mat3 TBN;
in vec4 material_v;
in vec3 campos;
uniform sampler2D albedo;
uniform sampler2D normal;
uniform sampler2D specular;
void main() {
	vec3 norm=normalize(normal_orig);
	if((textureUse&2)>0) {
		vec3 normal_tex=texture2D(normal,texCoords).xyz;
		normal_tex=normal_tex*2.0 - 1.0;
		normal_tex=vec3(-normal_tex.x,normal_tex.y,normal_tex.z);
		norm=normalize(TBN * normal_tex);
		//normal=TBN * normal_tex;
		//normal=normal+1.0f;
	}
	float spec=material_v.x;
	float usel=0;
	float roughness=material_v.y;
	if((textureUse&4)>0) {
		vec4 t=texture2D(specular,texCoords);
		spec=t.x;
		usel=t.z;
	}
	vec4 fcol=col;
	if((textureUse&1)>0) {
		fcol*=texture2D(albedo,texCoords);
	}
	
	pAlbedo=fcol;
	pSpecular=vec4(spec,useSSR,usel+(int(useLighting>=0)*(useLighting-usel)),1);
	pRoughness=vec4(roughness,0,view_position.z,1);
	gPosition=vec4(world_position,1);
	gNormal=vec4(norm,1);
}