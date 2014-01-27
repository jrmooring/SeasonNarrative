#version 130
in vec4 fPos;
out vec4 fColor;
uniform float time;
uniform float low;
uniform float med;
uniform float high;
uniform float mval;


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

    vec4 nPos = vec4(0);



    float treeFuzz = 0.0025;

    //cubes
    float sqsize = 4+sin(time*0.2);
    float refract = 0.0;
    vec4 dx = floor((rotate(fPos,time*0.01)-vec4(0,-1.0,0,0))*sqsize)/sqsize;
    if(dx.x == 0)
        dx.x = 0.000001;
    vec4 ddx = normalize(dx);
    nPos += ddx*length(dx)*refract;


    //blur
    nPos += fuzz(fPos,treeFuzz);

    vec4 tcolor = texture2D(tex,nPos.xy*0.5 + vec2(0.5,0.5)).rgba;

    //pre wind-spin fuzz
    nPos = fuzz(nPos, 0.1);

    //wind spin
    float rot = 0*sin(time);
    nPos = rotate(nPos,-rot*3*pow(2,-256*pow(1.0-length(nPos),2)));

    //post wind spin fuzz
    nPos = fuzz(nPos, 0.1);


    //background color points
    vec4 bgP1 = vec4(1.3, 1.3, 0, 0);
    vec4 bgC1 = vec4(0.3, 0.4, 0.8, 0);

    vec4 bgP2 = vec4(-1.3, 1.3, 0, 0);
    vec4 bgC2 = vec4(0.4, 0.3, 0.8, 0);

    vec4 bgP3 = vec4(0, -1.0, 0, 0);
    vec4 bgC3 = vec4(0.2, 0.2, 0.5, 0);

    vec4 bgP4 = vec4(0, 0, 0, 0);
    vec4 bgC4 = vec4(0.0, 0.0, 0.2, 0);


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
