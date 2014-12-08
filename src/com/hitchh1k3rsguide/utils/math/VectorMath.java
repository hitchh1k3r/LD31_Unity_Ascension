package com.hitchh1k3rsguide.utils.math;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

public class VectorMath
{

    public static class Vec2
    {

        public float x;
        public float y;

        public Vec2(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        public void add(Vec2 other)
        {
            x += other.x;
            y += other.y;
        }

        public void copy(Vec2 other)
        {
            this.x = other.x;
            this.y = other.y;
        }

    }

    public static class Vec3
    {

        private float x;
        private float y;
        private float z;
        private float w;

        public Vec3(float x, float y, float z, boolean isDirectionVector)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            if (isDirectionVector)
            {
                this.w = 0.0f;
            }
            else
            {
                this.w = 1.0f;
            }
        }

        public Vec3(float x, float y, float z)
        {
            this(x, y, z, false);
        }

        public Vec3 add(Vec3 other)
        {
            this.add(other.x, other.y, other.z);
            return this;
        }

        public void multiply(Matrix4 m)
        {
            float oX = x;
            float oY = y;
            float oZ = z;
            float oW = w;
            x = (m._elements[0] * oX) + (m._elements[4] * oY) + (m._elements[8] * oZ)
                    + (m._elements[12] * oW);
            y = (m._elements[1] * oX) + (m._elements[5] * oY) + (m._elements[9] * oZ)
                    + (m._elements[13] * oW);
            z = (m._elements[2] * oX) + (m._elements[6] * oY) + (m._elements[10] * oZ)
                    + (m._elements[14] * oW);
            w = (m._elements[3] * oX) + (m._elements[7] * oY) + (m._elements[11] * oZ)
                    + (m._elements[15] * oW);
        }

        public void add(float aX, float aY, float aZ)
        {
            x += aX;
            y += aY;
            z += aZ;
        }

        public void scale(float factor)
        {
            x *= factor;
            y *= factor;
            z *= factor;
        }

        public float sqMagnitude()
        {
            return (x * x) + (y * y) + (z * z);
        }

        public float magnitude()
        {
            return (float) Math.sqrt(sqMagnitude());
        }

        public float dot(Vec3 other)
        {
            return x * other.x + y * other.y + z * other.z;
        }

    }

    public static abstract class Matrix4
    {

        protected final float[] _elements = new float[16];

        public Matrix4()
        {
            for (int i = 0; i < 16; ++i)
            {
                this._elements[i] = 0.0f;
            }
        }

        public void setElement(int i, int j, float value)
        {
            this._elements[(4 * i) + j] = value;
        }

        public void writeToUniform(int uniformLocation)
        {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16).put(_elements);
            buffer.flip();
            GL20.glUniformMatrix4(uniformLocation, false, buffer);
        }

        public Matrix4 multiply(Matrix4 m)
        {
            float x1 = this._elements[0] * m._elements[0] + this._elements[4] * m._elements[1]
                    + this._elements[8] * m._elements[2] + this._elements[12] * m._elements[3];
            float x2 = this._elements[1] * m._elements[0] + this._elements[5] * m._elements[1]
                    + this._elements[9] * m._elements[2] + this._elements[13] * m._elements[3];
            float x3 = this._elements[2] * m._elements[0] + this._elements[6] * m._elements[1]
                    + this._elements[10] * m._elements[2] + this._elements[14] * m._elements[3];
            float x4 = this._elements[3] * m._elements[0] + this._elements[7] * m._elements[1]
                    + this._elements[11] * m._elements[2] + this._elements[15] * m._elements[3];

            float y1 = this._elements[0] * m._elements[4] + this._elements[4] * m._elements[5]
                    + this._elements[8] * m._elements[6] + this._elements[12] * m._elements[7];
            float y2 = this._elements[1] * m._elements[4] + this._elements[5] * m._elements[5]
                    + this._elements[9] * m._elements[6] + this._elements[13] * m._elements[7];
            float y3 = this._elements[2] * m._elements[4] + this._elements[6] * m._elements[5]
                    + this._elements[10] * m._elements[6] + this._elements[14] * m._elements[7];
            float y4 = this._elements[3] * m._elements[4] + this._elements[7] * m._elements[5]
                    + this._elements[11] * m._elements[6] + this._elements[15] * m._elements[7];

            float z1 = this._elements[0] * m._elements[8] + this._elements[4] * m._elements[9]
                    + this._elements[8] * m._elements[10] + this._elements[12] * m._elements[11];
            float z2 = this._elements[1] * m._elements[8] + this._elements[5] * m._elements[9]
                    + this._elements[9] * m._elements[10] + this._elements[13] * m._elements[11];
            float z3 = this._elements[2] * m._elements[8] + this._elements[6] * m._elements[9]
                    + this._elements[10] * m._elements[10] + this._elements[14] * m._elements[11];
            float z4 = this._elements[3] * m._elements[8] + this._elements[7] * m._elements[9]
                    + this._elements[11] * m._elements[10] + this._elements[15] * m._elements[11];

            float t1 = this._elements[0] * m._elements[12] + this._elements[4] * m._elements[13]
                    + this._elements[8] * m._elements[14] + this._elements[12] * m._elements[15];
            float t2 = this._elements[1] * m._elements[12] + this._elements[5] * m._elements[13]
                    + this._elements[9] * m._elements[14] + this._elements[13] * m._elements[15];
            float t3 = this._elements[2] * m._elements[12] + this._elements[6] * m._elements[13]
                    + this._elements[10] * m._elements[14] + this._elements[14] * m._elements[15];
            float t4 = this._elements[3] * m._elements[12] + this._elements[7] * m._elements[13]
                    + this._elements[11] * m._elements[14] + this._elements[15] * m._elements[15];

            this._elements[0] = x1;
            this._elements[1] = x2;
            this._elements[2] = x3;
            this._elements[3] = x4;
            this._elements[4] = y1;
            this._elements[5] = y2;
            this._elements[6] = y3;
            this._elements[7] = y4;
            this._elements[8] = z1;
            this._elements[9] = z2;
            this._elements[10] = z3;
            this._elements[11] = z4;
            this._elements[12] = t1;
            this._elements[13] = t2;
            this._elements[14] = t3;
            this._elements[15] = t4;

            return this;
        }

        public void printMatrix()
        {
            System.out.print("┌                            ┐\n");
            System.out.printf("| %+05f  %+05f  %+05f  %+05f |%n", _elements[0], _elements[4],
                    _elements[8], _elements[12]);
            System.out.printf("| %+05f  %+05f  %+05f  %+05f |%n", _elements[1], _elements[5],
                    _elements[9], _elements[13]);
            System.out.printf("| %+05f  %+05f  %+05f  %+05f |%n", _elements[2], _elements[6],
                    _elements[10], _elements[14]);
            System.out.printf("| %+05f  %+05f  %+05f  %+05f |%n", _elements[3], _elements[7],
                    _elements[11], _elements[15]);
            System.out.print("└                            ┘\n");
        }

        Matrix4 clone(int rows)
        {
            Matrix4 mat = null;
            if (rows == 3)
            {
                mat = new Matrix4x3(_elements[0], _elements[1], _elements[2], _elements[4],
                        _elements[5], _elements[6], _elements[8], _elements[9], _elements[10],
                        _elements[12], _elements[13], _elements[14]);
            }
            else if (rows == 4)
            {
                mat = new Matrix4x4(_elements[0], _elements[1], _elements[2], _elements[3],
                        _elements[4], _elements[5], _elements[6], _elements[7], _elements[8],
                        _elements[9], _elements[10], _elements[11], _elements[12], _elements[13],
                        _elements[14], _elements[15]);
            }
            return mat;
        }

    }

    public static class Matrix4x3 extends Matrix4
    {

        public Matrix4x3(float x1, float x2, float x3, float y1, float y2, float y3, float z1,
                float z2, float z3, float t1, float t2, float t3)
        {
            this._elements[0] = x1;
            this._elements[1] = x2;
            this._elements[2] = x3;
            this._elements[4] = y1;
            this._elements[5] = y2;
            this._elements[6] = y3;
            this._elements[8] = z1;
            this._elements[9] = z2;
            this._elements[10] = z3;
            this._elements[12] = t1;
            this._elements[13] = t2;
            this._elements[14] = t3;

            this._elements[3] = 0.0f;
            this._elements[7] = 0.0f;
            this._elements[11] = 0.0f;
            this._elements[15] = 1.0f;
        }

        public Matrix4x3()
        {
            super();
            this._elements[0] = 1.0f;
            this._elements[5] = 1.0f;
            this._elements[10] = 1.0f;
            this._elements[15] = 1.0f;
        }

        public Matrix4x3 inverse()
        {
            float it0 = -this._elements[12];
            float it1 = -this._elements[13];
            float it2 = -this._elements[14];

            float X = this._elements[0] * it0 + this._elements[1] * it1 + this._elements[2] * it2;
            float Y = this._elements[4] * it0 + this._elements[5] * it1 + this._elements[6] * it2;
            float Z = this._elements[8] * it0 + this._elements[9] * it1 + this._elements[10] * it2;
            return new Matrix4x3(this._elements[0], this._elements[4], this._elements[8],
                    this._elements[1], this._elements[5], this._elements[9], this._elements[2],
                    this._elements[6], this._elements[10], X, Y, Z);
        }

        Matrix4x3 interpolate(Matrix4x3 m, float partial)
        {
            Matrix4x3 result = new Matrix4x3();
            for (int i = 0; i < 16; ++i)
            {
                result._elements[i] = (this._elements[i] * (1.0f - partial))
                        + (m._elements[i] * partial);
            }
            return result;
        }

    }

    public static class Matrix4x4 extends Matrix4
    {

        public Matrix4x4(float x1, float x2, float x3, float x4, float y1, float y2, float y3,
                float y4, float z1, float z2, float z3, float z4, float t1, float t2, float t3,
                float t4)
        {
            this._elements[0] = x1;
            this._elements[1] = x2;
            this._elements[2] = x3;
            this._elements[3] = x4;
            this._elements[4] = y1;
            this._elements[5] = y2;
            this._elements[6] = y3;
            this._elements[7] = y4;
            this._elements[8] = z1;
            this._elements[9] = z2;
            this._elements[10] = z3;
            this._elements[11] = z4;
            this._elements[12] = t1;
            this._elements[13] = t2;
            this._elements[14] = t3;
            this._elements[15] = t4;
        }

        public Matrix4x4()
        {
            super();
            this._elements[0] = 1.0f;
            this._elements[5] = 1.0f;
            this._elements[10] = 1.0f;
            this._elements[15] = 1.0f;
        }

    }

    public static class MatrixStack
    {

        private final LinkedList<Matrix4x3> _modelStack = new LinkedList<Matrix4x3>();
        private final LinkedList<Matrix4x3> _viewStack = new LinkedList<Matrix4x3>();
        private final LinkedList<Matrix4x4> _projectionStack = new LinkedList<Matrix4x4>();

        public Matrix4x3 modelMatrix = new Matrix4x3();
        public Matrix4x3 viewMatrix = new Matrix4x3();
        public Matrix4x4 projectionMatrix = new Matrix4x4();

        public void pushModel()
        {
            _modelStack.push((Matrix4x3) modelMatrix.clone(3));
        }

        public void popModel()
        {
            modelMatrix = _modelStack.pop();
        }

        public void pushView()
        {
            _viewStack.push((Matrix4x3) viewMatrix.clone(3));
        }

        public void popView()
        {
            viewMatrix = _viewStack.pop();
        }

        public void pushProjection()
        {
            _projectionStack.push((Matrix4x4) projectionMatrix.clone(4));
        }

        public void popProjection()
        {
            projectionMatrix = _projectionStack.pop();
        }

    }

    public static Matrix4x3 translationMatrix(float x, float y, float z)
    {
        return new Matrix4x3(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, x, y, z);
    }

    public static Matrix4x3 scaleMatrix(float sX, float sY, float sZ)
    {
        return new Matrix4x3(sX, 0.0f, 0.0f, 0.0f, sY, 0.0f, 0.0f, 0.0f, sZ, 0.0f, 0.0f, 0.0f);
    }

    public static Matrix4x3 rotationMatrix(float angle, float x, float y, float z)
    {
        float invLen = (float) (1 / Math.sqrt(x * x + y * y + z * z));
        x *= invLen;
        y *= invLen;
        z *= invLen;

        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);
        float t = 1 - c;

        return new Matrix4x3(t * x * x + c, t * x * y + s * z, t * x * z - s * y,
                t * x * y - s * z, t * y * y + c, t * y * z + s * x, t * x * z + s * y, t * y * z
                        - s * x, t * z * z + c, 0.0f, 0.0f, 0.0f);
    }

    public static Matrix4x4 perspectiveMatrix(float fov, float aspect, float znear, float zfar)
    {
        float top = (float) (znear * Math.tan(fov * Math.PI / 360.0));
        float right = top * aspect;

        float X = znear / right;
        float Y = znear / top;
        float A = (znear - zfar) / (zfar - znear);
        float B = (-2 * zfar * znear) / (zfar - znear);

        return new Matrix4x4(X, 0.0f, 0.0f, 0.0f, 0.0f, Y, 0.0f, 0.0f, 0.0f, 0.0f, A, -1.0f, 0.0f,
                0.0f, B, 0.0f);
    }

    public static Matrix4x4 orthogonalMatrix(float xLeft, float xRight, float yTop, float yBottom,
            float zNear, float zFar)
    {
        float invWidth = 1.0f / (xRight - xLeft);
        float invHeight = 1.0f / (yTop - yBottom);
        float invDepth = 1.0f / (zNear - zFar);

        return new Matrix4x4(2.0f * invWidth, 0.0f, 0.0f, 0.0f, 0.0f, 2.0f * invHeight, 0.0f, 0.0f,
                0.0f, 0.0f, 2.0f * invDepth, 0.0f, -1.0f * (xLeft + xRight) * invWidth, -1.0f
                        * (yBottom + yTop) * invHeight, -1.0f * (zFar + zNear) * invDepth, 1.0f);
    }

    public static double sqDistanceToLine(Vec3 line1, Vec3 line2, Vec3 point)
    {
        Vec3 line = new Vec3(line2.x - line1.x, line2.y - line1.y, line2.z - line1.z);
        float length = line.magnitude();
        line.scale(1.0f / length);
        Vec3 projection = new Vec3(point.x - line1.x, point.y - line1.y, point.z - line1.z);
        float distance = projection.dot(line);
        Vec3 closest;
        if (distance < 0)
        {
            closest = new Vec3(line1.x, line1.y, line1.z);
        }
        else if (distance > length)
        {
            closest = new Vec3(line2.x, line2.y, line2.z);
        }
        else
        {
            line.scale(distance);
            closest = new Vec3(line1.x + line.x, line1.y + line.y, line1.z + line.z);
        }
        closest.add(-point.x, -point.y, -point.z);
        return closest.sqMagnitude();
    }

}
