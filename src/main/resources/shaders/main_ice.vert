#version 120

uniform float obstacleSizeFrac;
attribute float groundHeight;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    return gl_Vertex;
}

vec3 getNormal(mat4 transform)
{
    return (gl_ModelViewProjectionMatrix * vec4(gl_Normal, 0)).xyz;
}

void getVertVecs(out vec4 pos, out vec3 normal)
{
    pos = getPos(getTransform());
    normal = vec3(0, 0, 0);
}

vec4 getColor(vec4 colorIn)
{
    float pos = max(0.0, obstacleSizeFrac * 15.0 - groundHeight);
    float maxPos = 15.0 - groundHeight;

    return vec4(colorIn.xyz, colorIn.a * pos / maxPos);
}