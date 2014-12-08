#version 120

uniform vec2 spritePos;
uniform vec2 spriteSize;
uniform float zIndex;

attribute float vertIndex;

const float halfScreenWidth = 320.0 / 2.0;
const float halfScreenHeight = 240.0 / 2.0;
const float halfScreenDepth = 100.0 / 2.0;
const vec3 projectionVector = vec3(1.0 / halfScreenWidth, -1.0 / halfScreenHeight, -1.0 / halfScreenDepth);

vec3 posForVert(int vert)
{
	if(vert == 1)
	{
		return vec3(spritePos.x + spriteSize.x,
					spritePos.y,
					zIndex);
	}
	else if(vert == 2)
	{
		return vec3(spritePos.x + spriteSize.x,
					spritePos.y + spriteSize.y,
					zIndex);
	}
	else if(vert == 3)
	{
		return vec3(spritePos.x,
					spritePos.y + spriteSize.y,
					zIndex);
	}
	else
	{
		return vec3(spritePos.x,
					spritePos.y,
					zIndex);
	}
}

void main(void)
{

	gl_Position = vec4((posForVert(int(vertIndex)) - vec3(halfScreenWidth, halfScreenHeight, halfScreenDepth)) * projectionVector, 1.0);

}