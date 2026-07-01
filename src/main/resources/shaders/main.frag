#define DEPTH_OFFSET 0.0005

uniform sampler2D depthTexture;

varying vec4 lightBiasedClipPosition;

uniform sampler2D tex;

uniform bool texture;
uniform bool depthtest;
uniform float glow;

uniform int shadowres;
varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform float baseLight;
uniform float shadowLight;

uniform float width;
uniform float height;
uniform float depth;
uniform float scale;


#define BLEND_TRANSPARENT 0
#define BLEND_GLOW 1
#define BLEND_LIGHT 2

uniform int blendFunc;

void main(void)
{
    vec4 color = texture2D(tex, gl_TexCoord[0].st);
    vec4 fragColor;

    if (texture)
    {
        fragColor = color * vertexColor;

        if (color.a <= 0.0)
            discard;
    }
    else
        fragColor = vertexColor;

    if (vbo)
        fragColor *= originalColor;

    bool lit = true;
    float glowValue = 1.0;

    if (shadow)
    {
        vec4 lightNDCPosition = lightBiasedClipPosition / lightBiasedClipPosition.w;

        vec4 depthVec = texture2D(depthTexture, lightNDCPosition.xy);

        if (depthtest)
        {
            lit = depthVec.z >= lightNDCPosition.z - DEPTH_OFFSET * 2048.0 / float(shadowres);
            glowValue = glow;
        }
    }

    if (blendFunc == BLEND_GLOW || blendFunc == BLEND_LIGHT)
    {
        fragColor.rgb *= fragColor.a;
        gl_FragData[0] = vec4(0.0);
        gl_FragData[1] = fragColor * fragColor;
        gl_FragData[2] = vec4(0.0);
    }
    else
    {
        gl_FragData[0] = fragColor;
        gl_FragData[1] = vec4(fragColor.rgb * glowValue * (float(lit) * (1.0 - baseLight) + (1.0 - float(lit)) *  (1.0 - shadowLight)), fragColor.a);
        gl_FragData[2] = vec4(float(lit), 0.0, 0.0, fragColor.a);
    }
}
