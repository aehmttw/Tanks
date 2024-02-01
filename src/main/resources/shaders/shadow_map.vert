uniform sampler2D tex;
uniform bool texture;

void main(void)
{
    mat4 transform = getTransform();
    gl_Position = gl_ModelViewProjectionMatrix * getPos(transform);

    gl_TexCoord[0] = gl_MultiTexCoord0;
    //gl_TexCoord[1] = gl_MultiTexCoord1;
}