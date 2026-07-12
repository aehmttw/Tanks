#version 120

uniform float width;
uniform float height;

uniform sampler2D depthTex;

uniform mat4 invProjection;
uniform vec4 lightPos;
uniform vec3 lightColor;

void main()
{
    vec4 pos = gl_Vertex;

    gl_Position = gl_ModelViewProjectionMatrix * pos;
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
