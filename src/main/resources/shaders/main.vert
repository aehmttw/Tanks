
uniform mat4 lightViewProjectionMatrix;
uniform mat4 biasMatrix;
uniform mat4 inverseModelViewBaseMatrix;

uniform sampler2D depthTexture;

varying vec4 lightBiasedClipPosition;

uniform sampler2D tex;

uniform bool texture;
uniform bool depthtest;
uniform float glow;

uniform int shadowres;
varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;
uniform vec3 lightColor;

uniform int blendFunc;

uniform float baseLight;
uniform float shadowLight;

void main(void)
{
    vertexColor = getColor(gl_Color);

    vec4 pos;
    vec3 normal;

    getVertVecs(pos, normal);
    gl_Position = gl_ModelViewProjectionMatrix * pos;
    mat4 modelMatrix = inverseModelViewBaseMatrix * gl_ModelViewMatrix;
    lightBiasedClipPosition = biasMatrix * lightViewProjectionMatrix * modelMatrix * pos;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}

