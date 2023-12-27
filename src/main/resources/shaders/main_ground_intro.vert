#version 120
#define SIZE 25.0
#extension GL_EXT_gpu_shader4 : enable

attribute float groundHeight;
attribute float vertexCoord;

uniform float obstacleSizeFrac;
uniform float scale;
uniform bool d3;

#define SCALE

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    if (d3)
        return vec4(gl_Vertex.x, gl_Vertex.y, gl_Vertex.z + max(0.0, 2000.0 * (1.0 - obstacleSizeFrac * (1.0 + groundHeight / 10.0))), gl_Vertex.w);
    else
    {
        int coord = int(vertexCoord);
        float size = SIZE * (1.0 - obstacleSizeFrac);
        float coordX = float((coord & 1) * -2 + 1) * size;
        float coordY = float(((coord >> 1) & 1) * -2 + 1) * size;

        return vec4(gl_Vertex.x + coordX, gl_Vertex.y + coordY, gl_Vertex.z, gl_Vertex.w);
    }
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