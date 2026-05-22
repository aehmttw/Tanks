#version 120

uniform sampler2D colorTex;
uniform sampler2D lightTex;
uniform sampler2D glowTex;
uniform sampler2D shadowTex;

uniform vec3 baseLight;

void main()
{
    vec2 texPos = gl_TexCoord[0].st;

    vec3 lightFromLights = texture2D(lightTex, texPos).rgb;
    float intensity = dot(lightFromLights, vec3(0.2126, 0.7152, 0.0722)) + 0.00001;
    vec3 normalized = lightFromLights / intensity;

    gl_FragColor = vec4(texture2D(colorTex, texPos).rgb * (baseLight * texture2D(shadowTex, texPos).r + sqrt(lightFromLights)) + texture2D(glowTex, texPos).rgb, 1.0);

//    gl_FragColor = vec4(texture2D(colorTex, texPos).rgb * (baseLight.rgb + sqrt(intensity)), 0.5);
}
