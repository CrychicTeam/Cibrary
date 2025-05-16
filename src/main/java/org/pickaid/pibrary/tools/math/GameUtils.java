package org.pickaid.pibrary.tools.math;

import com.endertech.common.CommonMath;
import com.endertech.minecraft.forge.math.Vect3d;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.Random;

/**
 * Game mathematics utility class providing various mathematical calculations commonly used in game development
 * Including trigonometric functions, vector operations, random number generation, color processing, and complex number calculations
 *
 * 游戏数学工具类，提供各种游戏开发中常用的数学计算功能
 * 包括三角函数、向量运算、随机数生成、颜色处理和复数计算等
 */
public class GameUtils {
    public static final float PI = 3.1415927F;
    private static final Random random = new Random();

    /**
     * Calculate the cosine value of an angle
     * 计算角度的余弦值
     *
     * @param degrees angle in degrees
     * @return cosine value
     */
    public static float cos(float degrees) {
        return Mth.cos(getRadians(degrees));
    }

    /**
     * Calculate the sine value of an angle
     * 计算角度的正弦值
     *
     * @param degrees angle in degrees
     * @return sine value
     */
    public static float sin(float degrees) {
        return Mth.sin(getRadians(degrees));
    }

    /**
     * Check if two vectors are equal
     * 判断两个向量是否相等
     *
     * @param vec1 first vector
     * @param vec2 second vector
     * @return true if vectors are equal, false otherwise
     */
    public static boolean VectorsAreEqual(Vec3 vec1, Vec3 vec2) {
        if (vec1 != null && vec2 != null) {
            return vec1.x == vec2.x && vec1.y == vec2.y && vec1.z == vec2.z;
        } else {
            return vec1 == vec2;
        }
    }

    /**
     * Get the center point of a bounding box
     * 获取边界框的中心点
     *
     * @param bb bounding box
     * @return center point vector
     */
    public static Vect3d getBBCenter(AABB bb) {
        return bb != null ? Vect3d.from(CommonMath.getAverage(bb.minX, bb.maxX), CommonMath.getAverage(bb.minY, bb.maxY), CommonMath.getAverage(bb.minZ, bb.maxZ)) : Vect3d.ZERO;
    }

    /**
     * Convert radians to degrees
     * 将弧度转换为角度
     *
     * @param radians value in radians
     * @return value in degrees
     */
    public static float getDegrees(float radians) {
        return radians * 180.0F / PI;
    }

    /**
     * Convert degrees to radians
     * 将角度转换为弧度
     *
     * @param degrees value in degrees
     * @return value in radians
     */
    public static float getRadians(float degrees) {
        return degrees * PI / 180.0F;
    }

    /**
     * Calculate arctangent and convert to degrees
     * 计算反正切值并转换为角度
     *
     * @param y Y coordinate
     * @param x X coordinate
     * @return angle in degrees
     */
    public static float arcTan(double y, double x) {
        return getDegrees((float)Math.atan2(y, x));
    }

    /**
     * Round a value to specified precision
     * 将值舍入到指定精度
     *
     * @param value value to round
     * @param powerOf10 precision (power of 10)
     * @return rounded value
     */
    public static float roundTo(float value, int powerOf10) {
        double pow = Math.pow(10.0, (double)(-powerOf10));
        double mult = (double)value * pow;
        int round = Math.round((float)mult);
        return (float)((double)round / pow);
    }

    /**
     * Check if a vector contains NaN values
     * 检查向量是否包含NaN值
     *
     * @param vec vector to check
     * @return true if vector contains NaN, false otherwise
     */
    public static boolean isNaN(Vec3 vec) {
        return Double.isNaN(vec.x) || Double.isNaN(vec.y) || Double.isNaN(vec.z);
    }

    /**
     * Generate a random integer within specified range
     * 生成指定范围内的随机整数
     *
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return random integer
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Generate a random color
     * 生成随机颜色
     *
     * @return random color object
     */
    public static Color randomColor() {
        return Color.getColor(String.format("#%06x", random.nextInt(0x1000000)));
    }

    /**
     * Execute a callback function based on probability
     * 根据概率执行回调函数
     *
     * @param probability probability value (between 0-1)
     * @param callback callback function to execute
     * @throws IllegalArgumentException if probability is not between 0-1
     */
    public static void randomChance(float probability, Runnable callback) {
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        }
        if (random.nextFloat() <= probability) {
            callback.run();
        }
    }

    /**
     * Truncate a number to specified decimal places
     * 将数值截断到指定小数位
     *
     * @param num number to truncate
     * @param decimalPlaces number of decimal places
     * @return truncated number
     */
    public static float truncateTo(float num, int decimalPlaces) {
        float factor = (float) Math.pow(10, decimalPlaces);
        return (float) Math.floor(num * factor) / factor;
    }

    /**
     * Floor each component of a vector
     * 向下取整向量的各个分量
     *
     * @param pos original vector
     * @return new vector with floored components
     */
    public static Vec3 floorPos(Vec3 pos) {
        return new Vec3(Math.floor(pos.x), Math.floor(pos.y), Math.floor(pos.z));
    }

    /**
     * Convert an integer to a byte
     * 将整数转换为字节
     *
     * @param value integer value
     * @return byte value
     */
    public static byte toByte(int value) {
        return (byte) value;
    }

    /**
     * Providing polar and rectangular representations and conversions
     * 提供复数的极坐标和直角坐标表示及转换
     */
    public static class Complex {
        /**
         * Polar representation of a complex number
         * 复数的极坐标表示
         *
         * @param r     Magnitude
         *              模长
         * @param theta Phase angle (in radians)
         *              幅角（弧度）
         */
        public record Polar(double r, double theta) {}

        /**
         * Convert a complex number from rectangular to polar form
         * 将直角坐标形式的复数转换为极坐标形式
         *
         * @param real real part
         * @param imag imaginary part
         * @return complex number in polar form
         */
        public static Polar toPolar(double real, double imag) {
            double r = Math.sqrt(real * real + imag * imag);
            double theta = Math.atan2(imag, real);
            return new Polar(r, theta);
        }

        /**
         * Rectangular representation of a complex number
         * 复数的直角坐标表示
         *
         * @param real Real part
         *             实部
         * @param imag Imaginary part
         *             虚部
         */
        public record Rectangular(double real, double imag) {}

        /**
         * Convert a complex number from polar to rectangular form
         * 将极坐标形式的复数转换为直角坐标形式
         *
         * @param r magnitude
         * @param theta phase angle (in radians)
         * @return complex number in rectangular form
         */
        public static Rectangular toRectangular(double r, double theta) {
            double real = r * Math.cos(theta);
            double imag = r * Math.sin(theta);
            return new Rectangular(real, imag);
        }
    }

    /**
     * Calculate a new position based on angles and distance
     * 根据角度和距离计算新位置
     *
     * @param pos starting position
     * @param angleX pitch angle (in degrees)
     * @param angleY yaw angle (in degrees)
     * @param distance distance
     * @return new position vector
     */
    public static Vec3 getPosByAngleAndDistance(Vec3 pos, float angleX, float angleY, double distance) {
        double yawRad = getRadians(angleY);
        double pitchRad = getRadians(angleX);
        double newX = pos.x - distance * Math.sin(yawRad) * Math.cos(pitchRad);
        double newY = pos.y - distance * Math.sin(pitchRad);
        double newZ = pos.z + distance * Math.cos(yawRad) * Math.cos(pitchRad);
        return new Vec3(newX, newY, newZ);
    }

    /**
     * Represents rotation angles in a game
     * 表示游戏中的旋转角度
     *
     * @param yaw   Yaw angle (in degrees)
     *              偏航角（度）
     * @param pitch Pitch angle (in degrees)
     *              俯仰角（度）
     */
    public record Rotation(float yaw, float pitch) {}

    /**
     * Calculate the rotation needed to face from current position to target position
     * 计算从当前位置到目标位置所需的旋转角度
     *
     * @param currentPos current position
     * @param targetPos target position
     * @return rotation object
     */
    public static Rotation getRotationToTarget(Vec3 currentPos, Vec3 targetPos) {
        double deltaX = currentPos.x - targetPos.x;
        double deltaY = currentPos.y - targetPos.y;
        double deltaZ = currentPos.z - targetPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = getDegrees((float) Math.atan2(deltaX, deltaZ));
        float pitch = getDegrees((float) -Math.atan2(deltaY, horizontalDistance));

        return new Rotation(yaw, pitch);
    }

    /**
     * Calculate the Manhattan distance between two points
     * 计算两点间的曼哈顿距离
     *
     * @param p1 first point
     * @param p2 second point
     * @return Manhattan distance
     */
    public static double manhattanDistance(Vec3 p1, Vec3 p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
    }
}