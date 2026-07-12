uniform sampler2D tex;

uniform bool texture;

varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform int blendFunc;


void main(void)
{
    vertexColor = getColor(gl_Color);
    vec4 pos;
    vec3 normal;

    getVertVecs(pos, normal);

    gl_Position = gl_ModelViewProjectionMatrix * pos;

    gl_TexCoord[0] = gl_MultiTexCoord0;
}

