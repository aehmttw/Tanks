#version 120
#define SIZE 50.0
#define CENTER 25.0
#extension GL_EXT_gpu_shader4 : enable

uniform float obstacleSizeFrac;
attribute float groundHeight;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    float baseX = floor(gl_Vertex.x / SIZE) * SIZE;
    float baseY = floor(gl_Vertex.y / SIZE) * SIZE;
    float remX = gl_Vertex.x - baseX;
    float remY = gl_Vertex.y - baseY;

    bool edgeX = remX == 0.0;
    bool edgeY = remY == 0.0;
    float rest = CENTER * (1.0 - obstacleSizeFrac);

    float x = edgeX ? (gl_Vertex.x) : baseX + rest + obstacleSizeFrac * remX;
    float y = edgeY ? (gl_Vertex.y) : baseY + rest + obstacleSizeFrac * remY;
    float z = (edgeX || edgeY) ? gl_Vertex.z : obstacleSizeFrac * (gl_Vertex.z - groundHeight) + groundHeight;

    return vec4(x, y, z, gl_Vertex.w);
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