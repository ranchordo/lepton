#version 330 core

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BrightColor;

in vec2 texcoords;

uniform sampler2D screen;
uniform float bloom_thshld=1.0;
void main() {
	vec3 hdr=texture2D(screen,texcoords).xyz;
	FragColor=vec4(hdr,1);
	float brightness = dot(hdr, vec3(0.2126, 0.7152, 0.0722));
    if(brightness > bloom_thshld) {
        BrightColor = vec4(FragColor.rgb, 1.0);
    } else {
        BrightColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}	