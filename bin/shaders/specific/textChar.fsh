#version 430 core

layout (location = 0) out vec4 FragColor;

struct Info {
	vec4 info;
	vec4 texinfo;
	vec4 colinfo;
};

layout (std140) buffer info_buffer {
	Info infos[];
};

<<<<<<< Updated upstream
in vec2 texcoords;
in float instanceID;
=======
varying vec2 texcoords;
varying float instanceID;
>>>>>>> Stashed changes

uniform sampler2D tex;
void main() {
	Info info=infos[int(instanceID)];
	float c=texture2D(tex,texcoords).w;
	FragColor=vec4(info.colinfo.xyz,c);
}	