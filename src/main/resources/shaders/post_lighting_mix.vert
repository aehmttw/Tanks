#version 120

uniform sampler2D colorTex;
uniform sampler2D lightTex;

uniform vec3 baseLight;

void main()
{
    vec4 pos = gl_Vertex;

    gl_Position = gl_ModelViewProjectionMatrix * pos;
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
