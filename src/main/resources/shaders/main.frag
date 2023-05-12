#define DEPTH_OFFSET 0.0005
#define LIGHT_SCALE 32.0

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
varying vec4 position;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform float width;
uniform float height;
uniform float depth;
uniform float scale;

uniform int lightsCount;
uniform sampler2D lights;

float rescale(float f)
{
    return (1.0 - f) * (-LIGHT_SCALE / 2.0 + 0.5) + (f) * (LIGHT_SCALE / 2.0 + 0.5);
}

void main(void)
{
    vec4 color = texture2D(tex, gl_TexCoord[0].st);

    float glow2 = float((int(vertexColor.a)) / 2) / 255.0;
    float glow3 = max(glow, glow2);

    vec4 vertexColor2 = vec4(vertexColor);
    vertexColor2.a -= glow2 * 255.0 * 2.0;

    if (texture)
        gl_FragColor = color * vertexColor2;
    else
        gl_FragColor = vertexColor2;

    if (vbo)
        gl_FragColor *= originalColor;


    if (depthtest)
    {
        bool lit;

        if (shadow)
        {
            vec3 ratioPos = vec3(position.x * (width / height), position.y, position.z);
            float intensity = (dot(normalize(ratioPos), vec3(0, 0, 1)));

            vec4 lightNDCPosition = lightBiasedClipPosition / lightBiasedClipPosition.w;
            vec4 lightDepth = texture2D(depthTexture, lightNDCPosition.xy);

            lit = lightDepth.z >= lightNDCPosition.z - DEPTH_OFFSET * 2048.0 / float(shadowres);
        }
        else
            lit = true;
        /*float spotlightBrightness = 1.0;
        float spotlightDarkness = light * 4.0 - 3.0;
        float spotlightShadeBrightness = 0.75;
        float spotlightShadeDarkness = shade * 4.0 - 3.0;

        float spotlightLight = spotlightBrightness * intensity + spotlightDarkness * (1.0 - intensity);
        float spotlightShade = spotlightShadeBrightness * intensity + spotlightShadeDarkness * (1.0 - intensity);*/

        vec4 extraLight = vec4(0.0, 0.0, 0.0, 0.0);
        int c = 4;
        for (int i = 0; i < lightsCount; i++)
        {
            float off = 0.5 / 256.0;
            vec4 sub = vec4(off, off, off, 0);
            vec4 light = texture2D(lights, vec2((float(i * c)) / float(lightsCount * c), 0.0)) - sub;
            light += (texture2D(lights, vec2((float(i * c + 1)) / float(lightsCount * c), 0.0)) - sub) / 256.0;
            light += (texture2D(lights, vec2((float(i * c + 2)) / float(lightsCount * c), 0.0)) - sub) / 65536.0;
            float l = max(0.0, (light.w * 100000.0) / (pow((rescale(light.x) * width - position.x) / scale, 2.0) + pow((rescale(light.y) * height - position.y) / scale, 2.0) + pow((rescale(light.z) * depth - position.z) / scale, 2.0)));
            extraLight += l * (texture2D(lights, vec2((float(i * c + 3)) / float(lightsCount * c), 0.0)));
        }

        vec4 extraLightSqrt = vec4(sqrt(extraLight.x), sqrt(extraLight.y), sqrt(extraLight.z), 0.0);

        if (lit)
        {
            float col = light * (1.0 - glow3) + glowLight * glow3;
            gl_FragColor *= vec4(col, col, col, 1.0) + extraLightSqrt;
        }
        else
        {
            float col = shade * (1.0 - glow3) + glowShade * glow3;
            gl_FragColor *= vec4(col, col, col, 1.0) + extraLightSqrt;
        }

        //float fogFrac = pow(max(0.0, min(1.0, (position.w / depth - 0.2) / 0.8)), 5.0);
        //gl_FragColor.xyz = gl_FragColor.xyz * (1.0 - fogFrac) + vec3(0.8, 0.8, 0.8) * (fogFrac);
    }
    else if (depthtest)
    {
        float col = light * (1.0 - glow3) + glowLight * glow3;
        gl_FragColor *= vec4(col, col, col, 1.0);
    }
}