#version 120

uniform float width;
uniform float height;

uniform sampler2D depthTex;

uniform mat4 invProjection;
uniform vec4 lightPos;
uniform vec3 lightColor;

vec3 getWorldPos(vec2 uv)
{
    float depth = texture2D(depthTex, uv).r;

    vec4 clip = vec4(uv.x * 2.0 - 1.0, uv.y * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);

    vec4 view = invProjection * clip;
    view /= view.w;

    return view.xyz;
}

void main()
{
    vec3 worldPos = getWorldPos(vec2(gl_FragCoord.x / width, gl_FragCoord.y / height));
    vec3 dists = worldPos - lightPos.xyz;

    float dist = length(dists) / lightPos.w;

    float fade = max(1.0 - dist, 0.0);
    gl_FragColor = vec4(lightColor.rgb * lightColor.rgb * sign(lightColor.rgb) * fade * fade * fade, 1.0);
//    gl_FragColor = vec4(1, -0.5, 0, 1);
//    gl_FragColor.xyz = dists / dist / 1000.0 + vec3(0.5, 0.5, 0.5);
}
