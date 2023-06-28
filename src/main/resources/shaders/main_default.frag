#version 120

float getNormalLighting(float minLight, float maxLight, float frac)
{
    return minLight + (maxLight - minLight) * frac;
}