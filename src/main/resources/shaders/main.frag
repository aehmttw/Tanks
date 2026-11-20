#define DEPTH_OFFSET 0.002

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
uniform vec3 lightColor;

uniform bool drawToFramebuffer;

#define BLEND_TRANSPARENT 0
#define BLEND_GLOW 1
#define BLEND_LIGHT 2

uniform int blendFunc;

float shadowCompare(vec2 position, float depth, float bias)
{
    return step(depth - bias, texture2D(depthTexture, position).r);
}

float filteredShadow(vec2 position, float depth, float bias)
{
    vec2 halfTexel = vec2(0.5 / float(shadowres));
    return 0.25 * (
        shadowCompare(position + vec2(-halfTexel.x, -halfTexel.y), depth, bias) +
        shadowCompare(position + vec2(halfTexel.x, -halfTexel.y), depth, bias) +
        shadowCompare(position + vec2(-halfTexel.x, halfTexel.y), depth, bias) +
        shadowCompare(position + vec2(halfTexel.x, halfTexel.y), depth, bias));
}

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

    float lightAmount = 1.0;
    float glowValue = 1.0;

    if (shadow)
    {
        vec4 lightNDCPosition = lightBiasedClipPosition / lightBiasedClipPosition.w;

        if (depthtest)
        {
            bool inShadowMap = lightNDCPosition.x >= 0.0 && lightNDCPosition.x <= 1.0
                && lightNDCPosition.y >= 0.0 && lightNDCPosition.y <= 1.0
                && lightNDCPosition.z >= 0.0 && lightNDCPosition.z <= 1.0;

            if (inShadowMap)
            {
                float bias = DEPTH_OFFSET * 2048.0 / float(shadowres);
                lightAmount = filteredShadow(lightNDCPosition.xy, lightNDCPosition.z, bias);
            }

            glowValue = glow;
        }
    }

    if (drawToFramebuffer)
    {
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
            vec3 l = glowValue * (lightAmount * (vec3(1.0) - baseLight * lightColor) + (1.0 - lightAmount) * (vec3(1.0) - shadowLight * lightColor));
            gl_FragData[1] = vec4(fragColor.rgb * l, fragColor.a);
            gl_FragData[2] = vec4(lightAmount, 0.0, 0.0, fragColor.a);
        }
    }
    else
    {
        if (blendFunc == BLEND_GLOW || blendFunc == BLEND_LIGHT)
        {
            fragColor.rgb *= fragColor.a;
            gl_FragData[0] = fragColor * fragColor;
        }
        else
        {
            float maxLight = baseLight * (1.0 - glow) + glow;
            float minLight = shadowLight * (1.0 - glow) + glow;
            gl_FragData[0] = vec4(fragColor.rgb * mix(minLight, maxLight, lightAmount), fragColor.a);
        }
    }
}
