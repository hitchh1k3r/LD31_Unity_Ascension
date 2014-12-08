#version 120

uniform sampler2D texture0;

uniform int alpha;
uniform int color;

varying vec2 uvCoord;
varying vec3 screenPos;

vec3 lookupColor(int index)
{
	if(index == 0)
	{
		// black (bullets and stuff)
		return vec3(0.0, 0.0, 0.0);
	}
	else if(index == 1)
	{
		// blue
		return vec3(0.12, 0.47, 0.71);
	}
	else if(index == 2)
	{
		// green
		return vec3(0.20, 0.63, 0.17);
	}
	else if(index == 3)
	{
		// red
		return vec3(0.89, 0.10, 0.11);
	}
	else if(index == 4)
	{
		// orange
		return vec3(1.00, 0.50, 0.00);
	}
	else if(index == 5)
	{
		// purple
		return vec3(0.42, 0.24, 0.60);
	}
	else if(index == 6)
	{
		// brown
		return vec3(0.69, 0.35, 0.16);
	}
	else
	{
		// default (white)
		return vec3(1.0, 1.0, 1.0);
	}

	return vec3(0.0, 0.0, 0.0);

}

void main(void)
{

	vec4 colorVec = texture2D(texture0, uvCoord);

	if(colorVec.a < 0.01)
	{
		discard;
	}

	if(color > -1)
	{
		colorVec *= vec4(lookupColor(color), 1.0);
	}

    gl_FragColor = vec4(colorVec.rgb, (alpha == 1 ? colorVec.a : 1.0));

}