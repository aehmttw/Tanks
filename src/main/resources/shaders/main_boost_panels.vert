#version 120
#define SIZE 25.0
#define PI 3.1415926538
#extension GL_EXT_gpu_shader4 : enable

uniform int time;
uniform float obstacleSizeFrac;
attribute float vertexCoord;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    int coord = int(vertexCoord);
    float size = SIZE * (1.0 - obstacleSizeFrac);
    float coordX = float((coord & 1) * -2 + 1) * size;
    float coordY = float(((coord >> 1) & 1) * -2 + 1) * size;
    float coordZ = float(((coord >> 2) & 1) * -2 + 1) * size;

    return vec4(gl_Vertex.x + coordX, gl_Vertex.y + coordY, gl_Vertex.z + coordZ, gl_Vertex.w);
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
    float offset = (sin((colorIn.a * 255.0f * 50.0f + time / 300.0f * PI * 2) / 10.0f) + 1.0f) * 40.0f / 255.0f;
    return vec4((1.0f - offset / 2) * colorIn.r, min(1.0f, 0.7f - offset + colorIn.g) * colorIn.r, colorIn.g * colorIn.r, 1.0f);
}