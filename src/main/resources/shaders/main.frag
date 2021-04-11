#define DEPTH_OFFSET 0.0005

uniform sampler2D depthTexture;
uniform vec3 lightPosition;

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

void main(void)
{
    vec4 color = texture2D(tex, gl_TexCoord[0].st);

    if (texture)
        gl_FragColor = color * vertexColor;
    else
        gl_FragColor = vertexColor;

    if (vbo)
        gl_FragColor *= originalColor;

    if (shadow)
    {
        vec4 lightNDCPosition = lightBiasedClipPosition / lightBiasedClipPosition.w;

        vec4 depth = texture2D(depthTexture, lightNDCPosition.xy);

        if (!depthtest)
        gl_FragColor *= vec4(1.0, 1.0, 1.0, 1.0);
        else
        {
            bool lit = depth.z >= lightNDCPosition.z - DEPTH_OFFSET * 2048.0 / float(shadowres);

            if (lit)
            {
                float col = light * (1.0 - glow) + glowLight * glow;
                gl_FragColor *= vec4(col, col, col, 1.0);
            }
            else
            {
                float col = shade * (1.0 - glow) + glowShade * glow;
                gl_FragColor *= vec4(col, col, col, 1.0);
            }
        }
    }
}