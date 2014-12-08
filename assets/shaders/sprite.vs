#version 120

uniform vec2 spritePos;
uniform vec2 spriteSize;
uniform vec4 spriteUVs;
uniform float zIndex;
uniform float angle;
uniform vec2 rotationOffset;

attribute float vertIndex;

varying vec2 uvCoord;
varying vec3 screenPos;

const float halfScreenWidth = 320.0 / 2.0;
const float halfScreenHeight = 240.0 / 2.0;
const float halfScreenDepth = 100.0 / 2.0;
const vec3 projectionVector = vec3(1.0 / halfScreenWidth, -1.0 / halfScreenHeight, -1.0 / halfScreenDepth);

vec2 rotateVector(vec2 vec, float rads)
{
	return vec2(vec.x * cos(rads) - vec.y * sin(rads),
				vec.x * sin(rads) + vec.y * cos(rads));
}

vec3 posForVert(int vert)
{
	vec2 offset;
	if(vert == 1)
	{
		offset = vec2(spriteSize.x, 0);
	}
	else if(vert == 2)
	{
		offset = vec2(spriteSize.x, spriteSize.y);
	}
	else if(vert == 3)
	{
		offset = vec2(0, spriteSize.y);
	}
	else
	{
		offset = vec2(0, 0);
	}
	if(angle != 0)
	{
		offset = rotateVector(offset - rotationOffset, angle) + rotationOffset;
	}
	return vec3(spritePos + offset, zIndex);
}

vec2 uvForVert(int vert)
{
	if(vert == 1)
	{
		return vec2(spriteUVs[2], spriteUVs[1]);
	}
	else if(vert == 2)
	{
		return vec2(spriteUVs[2], spriteUVs[3]);
	}
	else if(vert == 3)
	{
		return vec2(spriteUVs[0], spriteUVs[3]);
	}
	else
	{
		return vec2(spriteUVs[0], spriteUVs[1]);
	}
}

void main(void)
{

	screenPos = posForVert(int(vertIndex));
	gl_Position = vec4((screenPos - vec3(halfScreenWidth, halfScreenHeight, halfScreenDepth)) * projectionVector, 1.0);
	uvCoord = uvForVert(int(vertIndex));

}