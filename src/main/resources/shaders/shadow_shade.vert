uniform mat4 viewProjectionMatrix;
uniform mat4 lightViewProjectionMatrix;
uniform mat4 biasMatrix;

varying vec4 lightBiasedClipPosition;

uniform sampler2D tex;

uniform bool texture;
uniform bool depthtest;
uniform float glow;

uniform float light;
uniform float glowLight;
uniform float shadow;
uniform float glowShadow;

uniform int shadowres;
varying vec4 vertexColor;

void main(void)
{
    vec3 worldPosition = gl_Vertex.xyz;

    lightBiasedClipPosition = biasMatrix * lightViewProjectionMatrix * vec4(worldPosition, 1.0);
    gl_Position = viewProjectionMatrix * vec4(worldPosition, 1.0);

    vertexColor = vec4(gl_Color.r, gl_Color.g, gl_Color.b, gl_Color.a);
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
}