#version 330 core

layout (location = 0) out vec4 FragColor;

in vec2 texcoords;
uniform mat4 world2view;
uniform mat4 proj_matrix;
uniform float ssrmix=0.4;

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;

#define STEPLEN 18.0

#define STEPS 35.0

uniform vec2 windowpixels=vec2(1,1);
vec2 pixel2UV(vec2 pixels) {
	return pixels/windowpixels;
}
ivec2 UV2pixel(vec2 uv) {
	return ivec2(windowpixels*uv);
}
void main() {
	vec3 hdr=texture2D(sampler0,texcoords).xyz;
	vec3 pos=texture2D(sampler1,texcoords).xyz;
	vec3 norm=texture2D(sampler2,texcoords).xyz;
	vec3 specinfo=texture2D(sampler3,texcoords).xyz;
	
	vec4 viewfrag=world2view*vec4(pos,1);
	vec3 viewnorm=mat3(world2view)*norm;
	vec3 cam2frag=viewfrag.xyz;
	vec3 marchdir=normalize(reflect(normalize(cam2frag),normalize(viewnorm)));
	
	vec3 startpos=viewfrag.xyz;
	vec3 endpos=viewfrag.xyz+marchdir;
	
	vec4 endproj=proj_matrix*vec4(endpos,1);
	endproj/=endproj.w;
	endproj.xy=endproj.xy*0.5+0.5;
	
	vec4 startproj=proj_matrix*vec4(startpos,1);
	startproj/=startproj.w;
	startproj.xy=startproj.xy*0.5+0.5;
	
	ivec2 startpix=UV2pixel(texcoords);//startproj.xy);
	ivec2 endpix=UV2pixel(endproj.xy);
	
	bool majorX=(abs(startpix.x-endpix.x)>abs(startpix.y-endpix.y));
	float signmaj=sign(int(majorX)*(endpix.x-startpix.x)+int(!majorX)*(endpix.y-startpix.y));
	float dzdmaj=(marchdir.z)/(int(majorX)*(endpix.x-startpix.x)+int(!majorX)*(endpix.y-startpix.y));
	float dmindmaj=(int(majorX)*(endpix.y-startpix.y)+int(!majorX)*(endpix.x-startpix.x))  /
			(int(majorX)*(endpix.x-startpix.x)+int(!majorX)*(endpix.y-startpix.y));
	vec2 pixstep=int(signmaj)*(int(majorX)*vec2(STEPLEN,STEPLEN*dmindmaj)+int(!majorX)*vec2(STEPLEN*dmindmaj,STEPLEN));
	float zperstep=dzdmaj*STEPLEN;
	
	float epsilon=0.00001;
	vec2 pixel=startpix;
	float z=viewfrag.z;
	bool hit=false;
	vec2 hitpixel=vec2(0,0);
	bool newhit=false;
	float dist=0;
	float pdist=dist;
	bool stillokay=true;
	float divisor=2;
	for(int i=0;i<STEPS;i++) {
		float mult=int(!hit)+(int(hit)*(-2*(int(newhit)-0.5))/divisor);
		pixel+=pixstep*mult;
		divisor+=int(hit)*2;
		z+=zperstep*mult;
		vec3 worldpos=texture2D(sampler1,pixel2UV(pixel)).xyz;
		vec3 pixnorm=texture2D(sampler2,pixel2UV(pixel)).xyz;
		vec3 viewpos=(world2view*vec4(worldpos,1)).xyz;
		dist=viewpos.z-z;
		stillokay=stillokay&&(abs(dist-pdist)<(max(0.8,5*abs(zperstep))) || pdist==0);
		newhit=((viewpos.z>(z+epsilon)) && (length(worldpos)>epsilon || length(pixnorm)>epsilon));
		newhit=newhit&&stillokay;
		hitpixel=hitpixel+int(newhit || hit)*(pixel-hitpixel);
		hit=hit||newhit;
		pdist=dist;
	}
	hit=hit&&stillokay;
	
	hit=hit&&(specinfo.y>0.5);
	vec2 mag=pixstep/STEPLEN;
	mag=max(mag,0);
	FragColor=vec4(((1-ssrmix)+ssrmix*int(!hit))*hdr+int(hit)*hdr*texture2D(sampler0,pixel2UV(hitpixel)).xyz,1);
}	