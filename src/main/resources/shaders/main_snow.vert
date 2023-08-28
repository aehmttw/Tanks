#version 120

uniform float obstacleSizeFrac;
uniform float shrubHeight;
attribute float groundHeight;
attribute vec3 groundColor;

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    return vec4(gl_Vertex.x, gl_Vertex.y, gl_Vertex.z * obstacleSizeFrac * shrubHeight + groundHeight, gl_Vertex.w);
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
    return colorIn * obstacleSizeFrac + vec4(groundColor, 1.0f) * (1.0 - obstacleSizeFrac);
}