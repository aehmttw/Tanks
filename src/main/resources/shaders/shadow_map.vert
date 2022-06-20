uniform mat4 viewProjectionMatrix;

uniform bool bonesEnabled;
uniform mat4 boneMatrices[128];
attribute vec4 bones;

void main(void)
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

        gl_Position = gl_ModelViewProjectionMatrix * (boneMatrices[bone1] * bone1w + boneMatrices[bone2] * bone2w + boneMatrices[bone3] * bone3w + boneMatrices[bone4] * bone4w) * gl_Vertex;
    }
    else
        gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
}