package com.hitchh1k3rsguide.utils.math;

public class CollisionChecks
{

    public static boolean AABBvAABB(float b1left, float b1top, float b1width, float b1height,
            float b2left, float b2top, float b2width, float b2height)
    {
        return (b1left < b2left + b2width && b2left < b1left + b1width && b1top < b2top + b2height && b2top < b1top
                + b1height);
    }

    public static boolean SegvAABB(VectorMath.Vec2 p1, VectorMath.Vec2 p2, float left, float top,
            float width, float height)
    {
        float xMin = p1.x;
        float xMax = p2.x;
        if (xMin > xMax)
        {
            xMin = p2.x;
            xMax = p1.x;
        }

        float dX = xMax - xMin;

        if (xMax > left + width)
        {
            xMax = left + width;
        }

        if (xMin < left)
        {
            xMin = left;
        }

        if (xMin > xMax)
        {
            return false;
        }

        float yMin = p1.y;
        float yMax = p2.y;
        if (yMin > yMax)
        {
            yMin = p2.y;
            yMax = p1.y;
        }

        if (dX > 0.000001f)
        {
            float slope = (yMax - yMin) / dX;
            float offset = yMin - slope * p1.x;
            yMin = slope * xMin + offset;
            yMax = slope * xMax + offset;
        }

        if (yMin > yMax)
        {
            float temp = yMin;
            yMin = yMax;
            yMax = temp;
        }

        if (yMax > top + height)
        {
            yMax = top + height;
        }

        if (yMin < top)
        {
            yMin = top;
        }

        if (yMin > yMax)
        {
            return false;
        }

        return true;
    }

}
