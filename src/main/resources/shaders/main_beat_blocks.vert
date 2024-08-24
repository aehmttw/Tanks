#version 120
#extension GL_EXT_gpu_shader4 : enable

uniform float obstacleSizeFrac;
uniform float outlineSizeFrac;
uniform float flashFrac;

attribute vec3 centerCoord;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    float size = obstacleSizeFrac;
    vec3 cc = centerCoord;

    if (cc.z < 0)
    {
        cc.z = -cc.z;
        size = outlineSizeFrac;
    }

    return vec4(gl_Vertex.xyz * size + cc * (1.0 - size), gl_Vertex.w);
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
    vec4 col = vec4(1, 1, 1, 1);
    if (centerCoord.z < 0)
        col = vec4(0, 0, 0, 1);

    return colorIn * (1.0 - flashFrac) + col * flashFrac;
}