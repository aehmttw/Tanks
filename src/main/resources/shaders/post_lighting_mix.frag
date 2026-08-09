#version 120

uniform sampler2D colorTex;
uniform sampler2D lightTex;
uniform sampler2D glowTex;
uniform sampler2D shadowTex;

uniform vec3 lightColor;
uniform float baseLight;
uniform float shadowLight;

void main()
{
    vec2 texPos = gl_TexCoord[0].st;

    vec3 lightFromLights = texture2D(lightTex, texPos).rgb;

    vec3 inputCol = texture2D(colorTex, texPos).rgb;
    float lit = texture2D(shadowTex, texPos).r;
    float intensity = lightColor.r * 0.2126 + lightColor.g * 0.7152 + lightColor.b * 0.0722;
    float sunIntensity = ((1.0 - lit) * shadowLight + lit * baseLight);
    float effectiveSunIntensity = sunIntensity * intensity;

    vec3 env = sunIntensity * lightColor;
    vec3 fromLights = sqrt(abs(lightFromLights)) * sign(lightFromLights);
    vec3 glow = texture2D(glowTex, texPos).rgb;

    float fOrig = 1.0 / (5.0 * effectiveSunIntensity + 1.0);
    float fLin = 1.0 - effectiveSunIntensity;

    float t = smoothstep(0.6, 1.0, effectiveSunIntensity);
    vec3 light = env + fromLights * mix(fLin, fOrig, t);

    gl_FragColor = vec4(inputCol * light + glow, 1.0);
}
