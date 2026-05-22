uniform sampler2D tex;

uniform bool texture;

varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform int blendFunc;


void main(void)
{
    vertexColor = gl_Color;
    vec4 pos = gl_Vertex;

    gl_Position = gl_ModelViewProjectionMatrix * pos;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}

