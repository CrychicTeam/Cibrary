package org.pickaid.pibrary.tools.math;

import java.util.Comparator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helper class for Vec3 operations in Minecraft
 * Minecraft中Vec3向量操作的辅助工具类
 */
public class Vec3Helper {
    /**
     * The directional angle in degrees between two vectors, between 0 and 360.
     * 计算两个向量之间的方向角（以度为单位），范围在0到360度之间。
     *
     * @param vec1 first vector 第一个向量
     * @param vec2 second vector 第二个向量
     * @param plane The approximate vector around which {@code vec1} was rotated to get {@code vec2}
     *              大致表示将vec1旋转得到vec2时的旋转轴向量
     * @return angle in degrees 角度值（度）
     */
    public static double angleBetween(Vec3 vec1, Vec3 vec2, Vec3 plane) {
        double angle = Math.acos(vec1.dot(vec2) / (vec1.length() * vec2.length())) * 180d / Mth.PI;
        if (vec1.dot(vec2.cross(plane)) < 0d) angle = 360d - angle;
        return angle;
    };

    /**
     * Rotates a vector around an axis by a specified angle
     * 将向量围绕指定轴旋转指定角度
     *
     * @param vec The vector to rotate 要旋转的向量
     * @param rotationAxis The vector about which to rotate the first vector 旋转轴向量
     * @param angle The angle in degrees through which to rotate the first vector around the second 旋转角度（度）
     * @return rotated vector 旋转后的向量
     */
    public static Vec3 rotate(Vec3 vec, Vec3 rotationAxis, double angle) {
        Vec3 k = rotationAxis.normalize();
        double angleInRads = angle * Mth.PI / 180d;
        return vec.scale(Math.cos(angleInRads))
                .add(k.cross(vec).scale(Math.sin(angleInRads)))
                .add(k.scale(k.dot(vec) * (1 - Math.cos(angleInRads))));
    };

    /**
     * Adds two Vec3i vectors together
     * 将两个Vec3i向量相加
     *
     * @param vec1 first vector 第一个向量
     * @param vec2 second vector 第二个向量
     * @return sum vector 向量和
     */
    public static Vec3i add(Vec3i vec1, Vec3i vec2) {
        return new Vec3i(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY(), vec1.getZ() + vec2.getZ());
    };

    /**
     * Creates a comparator that sorts vectors by their proximity to a target vector
     * 创建一个比较器，根据向量与目标向量的接近程度进行排序
     *
     * @param targetVector the target vector to compare against 用于比较的目标向量
     * @return comparator for sorting vectors 用于排序向量的比较器
     */
    public static Comparator<Vec3> getClosest(Vec3 targetVector) {
        return Comparator.comparingDouble(v -> v.dot(targetVector));
    };

    /**
     * Calculates the azimuth angle (horizontal angle) of a vector
     * 计算向量的方位角（水平角度）
     *
     * @param vec the vector 向量
     * @return azimuth angle in radians 方位角（弧度）
     */
    public static double azimuth(Vec3 vec) {
        return Math.atan2(vec.x, vec.z);
    };

    /**
     * Calculates the inclination angle (vertical angle) of a vector
     * 计算向量的倾角（垂直角度）
     *
     * @param vec the vector 向量
     * @return inclination angle in radians 倾角（弧度）
     */
    public static double inclination(Vec3 vec) {
        return Math.atan2(Math.sqrt(vec.x() * vec.x() + vec.z() * vec.z()), vec.y());
    };

    /**
     * Expands a bounding box to include a point
     * 扩展碰撞箱以包含指定点
     *
     * @param box the original bounding box 原始边界框
     * @param point the point to include 要包含的点
     * @return expanded bounding box 扩展后的边界框
     */
    public static AABB expandToInclude(AABB box, Vec3 point) {
        return new AABB(Math.min(box.minX, point.x), Math.min(box.minY, point.y), Math.min(box.minZ, point.z), Math.max(box.maxX, point.x), Math.max(box.maxY, point.y), Math.max(box.maxZ, point.z));
    };

    /**
     * Expands a bounding box to include a block position
     * 扩展碰撞箱以包含指定方块位置
     *
     * @param box the original bounding box 原始边界框
     * @param pos the block position to include 要包含的方块位置
     * @return expanded bounding box 扩展后的边界框
     */
    public static AABB expandToInclude(AABB box, BlockPos pos) {
        return expandToInclude(expandToInclude(box, Vec3.atLowerCornerOf(pos)), Vec3.atLowerCornerOf(pos).add(1d, 1d, 1d));
    };

    /**
     * Calculates the volume of a bounding box
     * 计算碰撞箱的体积
     *
     * @param box the bounding box 边界框
     * @return volume 体积
     */
    public static double volume(AABB box) {
        return (box.maxX - box.minX) * (box.maxY - box.minY) * (box.maxZ - box.minZ);
    };
}