#version 120

uniform int color;
uniform float alpha;

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
	else if(index == 7)
	{
		// white
		return vec3(1.0, 1.0, 1.0);
	}
	else if(index == 11)
	{
		// blue
		return vec3(0.02, 0.37, 0.61);
	}
	else if(index == 12)
	{
		// green
		return vec3(0.10, 0.53, 0.07);
	}
	else if(index == 13)
	{
		// red
		return vec3(0.79, 0.00, 0.01);
	}
	else if(index == 14)
	{
		// orange
		return vec3(0.90, 0.40, 0.00);
	}
	else if(index == 15)
	{
		// purple
		return vec3(0.32, 0.14, 0.50);
	}
	else if(index == 16)
	{
		// brown
		return vec3(0.59, 0.25, 0.06);
	}
	else if(index == 17)
	{
		// white
		return vec3(0.9, 0.9, 0.9);
	}
	else if(index == 8)
	{
		// dark
		return vec3(0.1, 0.1, 0.1);
	}
	else if(index == 9)
	{
		// health
		return vec3(0.9, 0.9, 0.9);
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

    gl_FragColor = vec4(lookupColor(color), alpha);

}