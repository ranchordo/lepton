#version 430 core

struct Info {
	vec4 info;
	vec4 texinfo;
	vec4 pieInfo1;
	vec4 pieInfo2;
};

layout (std140) buffer info_buffer {
	Info infos[];
};

in vec3 glv;
in vec2 mtc0;

out vec2 texcoords;
out float instanceID;
uniform mat4 proj_matrix;


void main() {
	instanceID=gl_InstanceID;
	Info info=infos[gl_InstanceID];
	gl_Position=proj_matrix*vec4((glv.x*info.info.z)+info.info.x,(glv.y*info.info.w)+info.info.y,-2.0,1.0);
	texcoords=mtc0;
}
