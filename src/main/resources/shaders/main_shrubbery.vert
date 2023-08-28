#version 120
#define SIZE 50.0
#define HALF_SIZE 25.0
#extension GL_EXT_gpu_shader4 : enable

uniform float obstacleSizeFrac;
uniform float shrubHeight;
attribute float vertexCoord;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    int coord = int(vertexCoord);
    float invFrac = 1.0 - obstacleSizeFrac;
    float size = HALF_SIZE * invFrac;
    float coordX = float((coord & 1) * -2 + 1) * size;
    float coordY = float(((coord >> 1) & 1) * -2 + 1) * size;
    float coordZ = float(((coord >> 2) & 1) * -2 + 1) * size;
    float h = (coord >> 3) * SIZE;
    float zDiff = gl_Vertex.z - h;

    return vec4(gl_Vertex.x + coordX, gl_Vertex.y + coordY, h + (zDiff * obstacleSizeFrac + HALF_SIZE * invFrac) * shrubHeight, gl_Vertex.w);
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
    return colorIn;
}