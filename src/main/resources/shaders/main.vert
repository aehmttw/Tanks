uniform mat4 viewProjectionMatrix;
uniform mat4 lightViewProjectionMatrix;
uniform mat4 biasMatrix;

varying vec4 lightBiasedClipPosition;

uniform sampler2D tex;

uniform bool texture;
uniform bool depthtest;
uniform float glow;

uniform float light;
uniform float glowLight;
uniform float shade;
uniform float glowShade;

uniform int shadowres;
varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform bool bonesEnabled;
uniform mat4 boneMatrices[128];
attribute vec4 bones;

void main(void)
{
    vertexColor = vec4(gl_Color.r, gl_Color.g, gl_Color.b, gl_Color.a);

    vec4 pos;

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

        pos = (boneMatrices[bone1] * bone1w + boneMatrices[bone2] * bone2w + boneMatrices[bone3] * bone3w + boneMatrices[bone4] * bone4w) * gl_Vertex;
    }
    else
        pos = gl_Vertex;

    gl_Position = gl_ModelViewProjectionMatrix * pos;
    lightBiasedClipPosition = biasMatrix * lightViewProjectionMatrix * gl_ModelViewMatrix * vec4(pos.xyz, 1.0);

    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
}

