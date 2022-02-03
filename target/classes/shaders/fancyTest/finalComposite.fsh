#version 330 core

layout (location = 0) out vec4 FragColor;

in vec2 texcoords;

uniform float exposure=1;
uniform float gamma=1;

#define MAXCOLOR 15.0
#define COLORS 16.0
#define WIDTH 256.0
#define HEIGHT 16.0

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
void main() {
	vec3 img=texture2D(sampler0,texcoords).xyz;
	vec3 bloom=texture2D(sampler1,texcoords).xyz;
	vec3 col=max(img+bloom,0.0);
	vec3 mapped=vec3(1.0)-exp(-col*exposure);
	col=pow(mapped,vec3(1.0/gamma));
	col=min(col,1);
	float cell=col.b*MAXCOLOR;
	float cell_l=floor(cell);
	float cell_h=ceil(cell);
	float half_px_x=0.5/WIDTH;
	float half_px_y=0.5/HEIGHT;
	float x_offset=max(2*half_px_x,half_px_x+col.r/COLORS*(MAXCOLOR/COLORS));
	float y_offset=half_px_y+col.g*(MAXCOLOR/COLORS);
	vec2 lut_pos_l=vec2(cell_l/COLORS+x_offset,1-y_offset);
	vec2 lut_pos_h=vec2(cell_h/COLORS+x_offset,1-y_offset);
	vec4 grcol_l=texture2D(sampler2,lut_pos_l);
	vec4 grcol_h=texture2D(sampler2,lut_pos_h);
	FragColor=mix(grcol_l,grcol_h,fract(cell));
}	