uniform sampler2D tex;

uniform bool texture;

varying vec4 vertexColor;

uniform bool shadow;

uniform bool vbo;
uniform vec4 originalColor;

uniform int blendFunc;

#define BLEND_TRANSPARENT 0
#define BLEND_GLOW 1
#define BLEND_LIGHT 2

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

    if (blendFunc == BLEND_GLOW || blendFunc == BLEND_LIGHT)
        fragColor.rgb *= fragColor.rgb * fragColor.a * fragColor.a;

    gl_FragColor = fragColor;
}
