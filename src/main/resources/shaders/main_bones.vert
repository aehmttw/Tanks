#version 120

uniform bool bonesEnabled;
uniform mat4 boneMatrices[128];
attribute vec4 bones;

mat3 toMat3(mat4 matrix)
{
    return mat3(matrix[0].xyz, matrix[1].xyz, matrix[2].xyz);
}

mat4 toMat4(mat3 matrix)
{
    return mat4(vec4(matrix[0].xyz, 0), vec4(matrix[1].xyz, 0), vec4(matrix[2].xyz, 0), vec4(0, 0, 0, 1));
}

mat3 transpose(mat3 mat)
{
    return mat3(vec3(mat[0].x, mat[1].x, mat[2].x), vec3(mat[0].y, mat[1].y, mat[2].y), vec3(mat[0].z, mat[1].z, mat[2].z));
}

float det(mat2 matrix)
{
    return matrix[0].x * matrix[1].y - matrix[0].y * matrix[1].x;
}

mat3 inverse(mat3 matrix)
{
    vec3 row0 = matrix[0];
    vec3 row1 = matrix[1];
    vec3 row2 = matrix[2];

    vec3 m0 = vec3(
    det(mat2(row1.y, row1.z, row2.y, row2.z)),
    det(mat2(row1.z, row1.x, row2.z, row2.x)),
    det(mat2(row1.x, row1.y, row2.x, row2.y)));

    vec3 m1 = vec3(
    det(mat2(row2.y, row2.z, row0.y, row0.z)),
    det(mat2(row2.z, row2.x, row0.z, row0.x)),
    det(mat2(row2.x, row2.y, row0.x, row0.y)));

    vec3 m2 = vec3(
    det(mat2(row0.y, row0.z, row1.y, row1.z)),
    det(mat2(row0.z, row0.x, row1.z, row1.x)),
    det(mat2(row0.x, row0.y, row1.x, row1.y)));

    mat3 adj = transpose(mat3(m0, m1, m2));

    return (1.0 / dot(row0, m0)) * adj;
}

mat4 getTransform()
{
    if (bonesEnabled)
    {
        int bone1 = int(bones.x - 0.0001);
        int bone2 = int(bones.y - 0.0001);
        int bone3 = int(bones.z - 0.0001);
        int bone4 = int(bones.w - 0.0001);
        float bone1w = bones.x - float(bone1);
        float bone2w = bones.y - float(bone2);
        float bone3w = bones.z - float(bone3);
        float bone4w = bones.w - float(bone4);

        return (boneMatrices[bone1] * bone1w + boneMatrices[bone2] * bone2w + boneMatrices[bone3] * bone3w + boneMatrices[bone4] * bone4w);
    }
    else
        return mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
}

vec4 getPos(mat4 transform)
{
    return transform * gl_Vertex;
}

vec3 getNormal(mat4 transform)
{
    if (bonesEnabled)
        return normalize(transpose(toMat3(transform * toMat4(inverse(toMat3(gl_ModelViewMatrix))))) * gl_Normal);
    else
        return gl_Normal;
}

void getVertVecs(out vec4 pos, out vec3 normal)
{
    mat4 transform = getTransform();
    pos = getPos(transform);
    normal = getNormal(transform);
}

