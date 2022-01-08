#version 430 core
#define PI 3.141592654

layout (location = 0) out vec4 FragColor;

struct Info {
	vec4 info;
	vec4 texinfo;
	vec4 pieInfo1;
	vec4 pieInfo2;
};

layout (std140) buffer info_buffer {
	Info infos[];
};

in vec2 texcoords;
in float instanceID;

void main() {
	Info info=infos[int(instanceID)];
	vec2 texcoordsl=texcoords-vec2(0.5,0.5);
	float ang=atan(texcoordsl.y,texcoordsl.x);
	ang/=2*PI;
	ang=fract(ang+0.25);
	bool c=((ang>=info.pieInfo1.x) && (ang<=(info.pieInfo1.x+info.pieInfo1.y)) && (length(texcoordsl)<=0.5));
	FragColor=vec4(info.pieInfo2.xyz,int(c));
}	
