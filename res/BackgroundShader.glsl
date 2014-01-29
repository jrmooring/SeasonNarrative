#version 130
in vec4 fPos;
out vec4 fColor;
uniform float time;
uniform float bgFuzz;
uniform float fgFuzz;
uniform float high;
uniform float wind;
uniform float sqRefract;


uniform sampler2D tex;

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec4 fuzz(vec4 pos, float offset){
    float r =  rand(pos.xy+time*0.001)*2 - 1;
    float r2 = rand(pos.xy-time*0.001)*2 - 1;
    return pos + offset*vec4(r,r2,0,0);
}

vec4 rotate(vec4 v, float r){
    return vec4(v.x*cos(r)-v.y*sin(r),v.x*sin(r)+v.y*cos(r),0,0);
}

void main(void) {

    vec4 ffPos = fPos;
    ffPos.y *= 8./12;

    vec4 nPos = vec4(0);



    float treeFuzz = 0.0025;

    //cubes
    float sqsize = 3+sin(time*0.2);
    vec4 dx = floor((rotate(ffPos,time*0.01)-vec4(0,-1.0,0,0))*sqsize)/sqsize;
    if(dx.x == 0)
        dx.x = 0.000001;
    vec4 ddx = normalize(dx);
    nPos -= ddx*length(dx)*sqRefract;


    //blur
    nPos += fuzz(ffPos,treeFuzz*fgFuzz);

    vec4 tcolor = texture2D(tex,nPos.xy*0.5 + vec2(0.5,0.35)).rgba;

    //pre wind-spin fuzz
    nPos = fuzz(nPos, 0.1);

    //wind spin
    vec4 spinCenter = vec4(wind*0.5,-0.23,0,0);
    nPos = rotate(nPos,-wind*1*pow(2,-256*pow(1.1+abs(wind)*0.05-distance(nPos,spinCenter),2)));

    //post wind spin fuzz
    nPos = fuzz(nPos, 0.1*bgFuzz);


    //background color points
    vec4 bgP1 = vec4(1.1, 1.1, 0, 0);
    vec4 bgC1 = vec4(0.4, 0.5, 0.9, 0);

    vec4 bgP2 = vec4(-1.1, 1.1, 0, 0);
    vec4 bgC2 = vec4(0.5, 0.4, 0.9, 0);

    vec4 bgP3 = vec4(0, -1.7, 0, 0);
    vec4 bgC3 = vec4(0.2, 0.2, 0.5, 0);

    vec4 bgP4 = vec4(0, -0.2, 0, 0);
    vec4 bgC4 = vec4(0.7, 0.7, 0.2, 0);


    //background color blending
    vec4 bcolor = 0.2*abs(2 - distance(nPos,bgP1))*bgC1;
    bcolor += 0.2*abs(2 - distance(nPos,bgP2))*bgC2;
    bcolor += 0.2*abs(2 - distance(nPos,bgP3))*bgC3;
    bcolor += 0.2*abs(2 - distance(nPos,bgP4))*bgC4;
    bcolor.a = 1;


    //merge tree with background
    if(tcolor.rgb == vec3(0))
        tcolor.a = 0;
    vec4 tbColor = tcolor*tcolor.a + bcolor*(1-tcolor.a);

    fColor = tbColor;

}
