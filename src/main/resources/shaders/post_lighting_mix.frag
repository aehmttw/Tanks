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
//    float intensity = dot(lightFromLights, vec3(0.2126, 0.7152, 0.0722)) + 0.00001;
//    vec3 normalized = lightFromLights / intensity;

    vec3 inputCol = texture2D(colorTex, texPos).rgb;
    vec2 sunInfo = texture2D(shadowTex, texPos).rg;
    float sunIntensity = ((1.0 - sunInfo.r) * shadowLight + sunInfo.r * baseLight) * (1.0 - sunInfo.g) + sunInfo.g;

    vec3 env = sunIntensity * lightColor;
    vec3 fromLights = sqrt(abs(lightFromLights)) * sign(lightFromLights);
    vec3 glow = texture2D(glowTex, texPos).rgb;
    float sunAdj = 5.0 * sunIntensity + 1.0;

    vec3 light = env + fromLights / sunAdj;

    gl_FragColor = vec4(inputCol * light + glow, 1.0);
//    gl_FragColor.xyz = gl_FragColor.xyz * 0.1 + lightFromLights;
//    gl_FragColor = vec4(texture2D(colorTex, texPos).rgb * (baseLight.rgb + sqrt(intensity)), 0.5);
}
