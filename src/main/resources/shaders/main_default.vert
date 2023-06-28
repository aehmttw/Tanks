#version 120

void getVertVecs(out vec4 pos, out vec3 normal)
{
    pos = gl_Vertex;
    normal = gl_Normal;
}

vec4 getPos(mat4 transform)
{
    return gl_Vertex;
}

vec3 getNormal(mat4 transform)
{
    return (gl_ModelViewProjectionMatrix * vec4(gl_Normal, 0)).xyz;
}

mat4 getTransform()
{
    return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}