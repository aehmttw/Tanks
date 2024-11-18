#version 120

attribute float timeOffset;
attribute float posOffset;
attribute float maxAge;

uniform float time;
uniform vec3 gravity;
uniform vec4 color;

vec3 getNormal(mat4 transform)
{
    return vec3(0, 0, 0);
}

vec4 getPos(mat4 transform)
{
    return gl_Vertex;
}

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

void getVertVecs(out vec4 pos, out vec3 normal)
{
    float timeLength = 20.0;
    float t = max(time - timeOffset, 0.0);
    float frac = max(0.0, maxAge - time) * max((timeLength - timeOffset) / timeLength, 0);
    vec3 v = gl_Vertex.xyz + gravity * t;
    pos = vec4(gl_Vertex.xyz * t + 0.5 * gravity * t * t + cross(normalize(v), vec3(0, 0, posOffset)) * frac / 80.0, 1.0);
    normal = vec3(0, 0, 0);
}

vec4 getColor(vec4 colorIn)
{
    return vec4((color.rgb + colorIn.rgb * colorIn.a) * (1.0 - ((time / maxAge) * max((60.0 - timeOffset) / 60.0, 0))), 1.0);
}