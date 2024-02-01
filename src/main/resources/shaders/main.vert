//uniform vec3 lightVec;

uniform mat4 lightViewProjectionMatrix;
uniform mat4 biasMatrix;

varying vec4 lightBiasedClipPosition;

//uniform bool customLight;
//uniform vec3 lightDiffuse;
//uniform vec3 lightAmbient;
//uniform vec3 lightSpecular;
//uniform float shininess;

uniform float minBrightness;
uniform float maxBrightness;
uniform bool negativeBrightness;

uniform sampler2D tex;

uniform bool texture;
uniform bool depthtest;
uniform float glow;

uniform float light;
uniform float glowLight;
uniform float shade;
uniform float glowShade;

//uniform float edgeLight;
//uniform float edgeCutoff;
//
//uniform float celsections;

uniform int shadowres;
varying vec4 vertexColor;
varying vec4 position;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

//uniform bool useNormal;
//varying vec3 normal;

uniform float width;
uniform float height;
uniform float depth;

#ifndef SCALE
uniform float scale;
#endif

uniform int lightsCount;
uniform sampler2D lightsTexture;

//uniform bool bonesEnabled;
//uniform mat4 boneMatrices[128];
//attribute vec4 bones;

void main(void)
{
    vertexColor = getColor(gl_Color);

    vec4 pos;
    vec3 normal;

    getVertVecs(pos, normal);

    gl_Position = gl_ModelViewProjectionMatrix * pos;
    lightBiasedClipPosition = biasMatrix * lightViewProjectionMatrix * gl_ModelViewMatrix * vec4(pos.xyz, 1.0);
    position = gl_ModelViewMatrix * pos;

    gl_TexCoord[0] = gl_MultiTexCoord0;
    //gl_TexCoord[1] = gl_MultiTexCoord1;
}

