#version 120

attribute vec2 offset;
attribute float maxAge;

uniform float time;
uniform vec3 gravity;
uniform vec4 color;
uniform float fireworkGlow;

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
    float frac = max(0.0, maxAge - time * (1.0 - fireworkGlow));
    pos = vec4(gl_Vertex.xyz * time + 0.5 * gravity * time * time + vec3(offset, 0.0) * (1 + fireworkGlow * 7) * frac / 80.0, 1.0);
    normal = vec3(0, 0, 0);
}

vec4 getColor(vec4 colorIn)
{
    return vec4((color.rgb + colorIn.rgb) * (1.0 - fireworkGlow * colorIn.a) * (1.0 - fireworkGlow * (time / maxAge)), 1.0 - (time / maxAge));
}