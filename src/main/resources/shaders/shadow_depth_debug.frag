uniform sampler2D tex;

uniform bool texture;

varying vec4 vertexColor;

uniform bool vbo;
uniform vec4 originalColor;

uniform int blendFunc;

void main(void)
{
    vec4 color = texture2D(tex, gl_TexCoord[0].st);
    float depth = clamp(color.r, 0.0, 1.0);

    float nearView = 1.0 - depth;
    float boosted = pow(nearView, 0.18);
    float bands = floor(boosted * 8.0) / 8.0;

    vec3 low = vec3(0.02, 0.02, 0.08);
    vec3 mid = vec3(0.0, 0.75, 1.0);
    vec3 high = vec3(1.0, 0.95, 0.15);
    vec3 hot = vec3(1.0, 0.2, 0.05);

    vec3 cool = mix(low, mid, smoothstep(0.0, 0.45, boosted));
    vec3 warm = mix(high, hot, smoothstep(0.6, 1.0, boosted));
    vec3 colorized = mix(cool, warm, smoothstep(0.35, 0.75, boosted));
    colorized = mix(colorized, vec3(bands), 0.2);

    vec4 fragColor = texture ? vec4(colorized, 1.0) * vertexColor : vertexColor;

    if (vbo)
        fragColor *= originalColor;

    if (blendFunc != 0)
        fragColor.rgb *= fragColor.rgb * fragColor.a * fragColor.a;

    gl_FragColor = vec4(fragColor.rgb, 1.0);
}
