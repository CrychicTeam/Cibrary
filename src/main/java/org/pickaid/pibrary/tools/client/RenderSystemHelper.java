package org.pickaid.pibrary.tools.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class RenderSystemHelper {
    public static final class Colors {
        public static final int WHITE = 0xFFFFFFFF;
        public static final int BLACK = 0xFF000000;
        public static final int RED = 0xFFFF0000;
        public static final int GREEN = 0xFF00FF00;
        public static final int BLUE = 0xFF0000FF;
        public static final int YELLOW = 0xFFFFFF00;
        public static final int MAGENTA = 0xFFFF00FF;
        public static final int CYAN = 0xFF00FFFF;
        public static final int ORANGE = 0xFFFF8000;
        public static final int PURPLE = 0xFF8000FF;
        public static final int PINK = 0xFFFF80C0;
        public static final int LIME = 0xFF80FF00;

        public static final int TRANS_WHITE = 0x80FFFFFF;
        public static final int TRANS_BLACK = 0x80000000;
        public static final int TRANS_RED = 0x80FF0000;
        public static final int TRANS_BLUE = 0x800000FF;
    }

    /**
     * Pre-configured Vector3f lighting directions that can be used with setShaderLights.
     * <p>
     * 预配置的Vector3f光照方向，可用于setShaderLights。
     */
    public static final class Lighting {
        public static final Vector3f TOP_LEFT = new Vector3f(0.2F, 1.0F, -0.7F);
        public static final Vector3f TOP_RIGHT = new Vector3f(-0.2F, 1.0F, 0.7F);
        public static final Vector3f FLAT_TOP = new Vector3f(0.0F, 1.0F, 0.0F);
        public static final Vector3f FRONT = new Vector3f(0.0F, 0.0F, 1.0F);
        static {
            TOP_LEFT.normalize();
            TOP_RIGHT.normalize();
            FLAT_TOP.normalize();
            FRONT.normalize();
        }
    }

    private static final int GL_ZERO = 0;
    private static final int GL_ONE = 1;
    private static final int GL_SRC_COLOR = 768;
    private static final int GL_ONE_MINUS_SRC_COLOR = 769;
    private static final int GL_SRC_ALPHA = 770;
    private static final int GL_ONE_MINUS_SRC_ALPHA = 771;
    private static final int GL_DST_ALPHA = 772;
    private static final int GL_ONE_MINUS_DST_ALPHA = 773;
    private static final int GL_DST_COLOR = 774;
    private static final int GL_ONE_MINUS_DST_COLOR = 775;



    /**
     * 设置标准的alpha混合（源alpha，1减源alpha）。
     * 这是最常见的混合模式，用于半透明物体（如玻璃、水等）的渲染。
     * 它会根据源像素的alpha值确定源像素和目标像素的混合比例。
     *
     * Sets up standard alpha blending (source alpha, one minus source alpha).
     * This is the most common blending mode, used for rendering semi-transparent objects (like glass, water, etc).
     * It determines the mixing ratio of source and destination pixels based on the source pixel's alpha value.
     */
    public static void setupAlphaBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    /**
     * 设置加法混合（源alpha，1）。
     * 这种混合模式会使重叠区域的颜色更亮，因为它将源像素的颜色添加到目标像素上。
     * 适用于发光效果，如火焰、光芒、魔法粒子等。
     *
     * Sets up additive blending (source alpha, one).
     * This blending mode makes overlapping areas brighter by adding the source pixel color to the destination pixel.
     * Used for glow effects, like fire, light beams, magic particles, etc.
     */
    public static void setupAdditiveBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
    }

    /**
     * 设置乘法混合（目标颜色，0）。
     * 这种混合模式通过将源像素与目标像素的颜色相乘来使图像变暗。
     * 适用于阴影、遮罩等暗化效果。
     *
     * Sets up multiplicative blending (destination color, zero).
     * This blending mode darkens the image by multiplying the source pixel color with the destination pixel color.
     * Used for shadows, masks, and darkening effects.
     */
    public static void setupMultiplicativeBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.DST_COLOR, DestFactor.ZERO);
    }

    /**
     * 推送所有应该为UI渲染保存的渲染状态。
     * 此方法将保存当前渲染状态并设置适合UI渲染的新状态：
     * - 禁用面剔除（让正反面都能渲染）
     * - 启用混合（透明度处理）
     * - 设置默认混合函数
     * - 设置着色器颜色为白色
     * - 备份投影矩阵（为UI渲染设置正交投影）
     *
     * Pushes all render states that should be preserved for UI rendering.
     * This method saves the current rendering state and sets up new states suitable for UI rendering:
     * - Disables face culling (allowing both sides to render)
     * - Enables blending (for transparency handling)
     * - Sets default blend function
     * - Sets shader color to white
     * - Backs up the projection matrix (to set orthographic projection for UI)
     */
    public static void pushUIRenderState() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.backupProjectionMatrix();
    }

    /**
     * 在UI渲染后弹出所有渲染状态。
     * 恢复之前通过pushUIRenderState()保存的投影矩阵和其他渲染状态。
     * 应该在UI渲染完成后调用，以确保3D世界渲染时使用正确的状态。
     *
     * Pops all render states after UI rendering.
     * Restores the projection matrix and other rendering states previously saved by pushUIRenderState().
     * Should be called after UI rendering is complete to ensure proper states for 3D world rendering.
     */
    public static void popUIRenderState() {
        RenderSystem.restoreProjectionMatrix();
    }

    /**
     * 在指定槽位设置着色器纹理。
     * 纹理是应用到3D模型表面的图像。现代GPU允许同时绑定多个纹理（多达16个）。
     * 不同槽位可用于不同目的，如：基础颜色（槽位0）、法线贴图（槽位1）、高光贴图（槽位2）等。
     *
     * @param slot 纹理槽位（0-11）- 当前实现限制为最多12个槽位
     * @param texture 纹理资源位置 - 指向纹理文件的资源标识符
     * @throws IllegalArgumentException 如果槽位不在0-11范围内
     *
     * Sets the shader texture at the specified slot.
     * Textures are images applied to 3D model surfaces. Modern GPUs allow binding multiple textures simultaneously (up to 16).
     * Different slots can be used for different purposes, such as: params color (slot 0), normal maps (slot 1), specular maps (slot 2), etc.
     *
     * @param slot The texture slot (0-11) - current implementation limits to maximum 12 slots
     * @param texture The texture resource location - resource identifier pointing to the texture file
     * @throws IllegalArgumentException if the slot is not in the 0-11 range
     */
    public static void setTexture(int slot, ResourceLocation texture) {
        if (slot < 0 || slot > 11) {
            throw new IllegalArgumentException("Texture slot must be between 0 and 11");
        }
        RenderSystem.setShaderTexture(slot, texture);
    }

    /**
     * 设置着色器，并进行错误检查。
     * 着色器是在GPU上运行的程序，用于确定如何渲染几何体。
     * 它们控制顶点变换（顶点着色器）和像素颜色计算（片段着色器）。
     *
     * @param shaderSupplier 着色器提供器 - 提供着色器实例的函数接口
     * @throws IllegalArgumentException 如果着色器提供器为null
     *
     * Sets shader with error checking.
     * Shaders are programs that run on the GPU to determine how geometry is rendered.
     * They control vertex transformations (vertex shader) and pixel color calculations (fragment shader).
     *
     * @param shaderSupplier The shader supplier - functional interface that provides the shader instance
     * @throws IllegalArgumentException if the shader supplier is null
     */
    public static void setShader(Supplier<ShaderInstance> shaderSupplier) {
        if (shaderSupplier == null) {
            throw new IllegalArgumentException("Shader supplier cannot be null");
        }
        RenderSystem.setShader(shaderSupplier);
    }

    /**
     * 使用0.0到1.0之间的浮点值设置着色器颜色。
     * 该颜色会与顶点颜色和纹理颜色相乘，影响最终渲染的颜色。
     * 常用于全局着色效果，如淡入淡出、变色等。
     *
     * @param red 红色分量 (0.0-1.0) - 红色通道的强度
     * @param green 绿色分量 (0.0-1.0) - 绿色通道的强度
     * @param blue 蓝色分量 (0.0-1.0) - 蓝色通道的强度
     * @param alpha 透明度分量 (0.0-1.0) - 透明度通道的强度，1.0为完全不透明，0.0为完全透明
     *
     * Sets shader color with values between 0.0 and 1.0.
     * This color is multiplied with vertex colors and texture colors, affecting the final rendered color.
     * Commonly used for global coloring effects such as fading in/out, tinting, etc.
     *
     * @param red Red component (0.0-1.0) - intensity of the red channel
     * @param green Green component (0.0-1.0) - intensity of the green channel
     * @param blue Blue component (0.0-1.0) - intensity of the blue channel
     * @param alpha Alpha component (0.0-1.0) - intensity of the transparency channel, 1.0 is fully opaque, 0.0 is fully transparent
     */
    public static void setColor(float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    /**
     * 使用0到255之间的RGB整数值设置着色器颜色。
     * 这是一个便捷方法，允许使用更常见的0-255整数范围来指定颜色。
     * 内部会将这些值转换为0.0-1.0范围的浮点数。
     *
     * @param red 红色分量 (0-255) - 红色通道的强度
     * @param green 绿色分量 (0-255) - 绿色通道的强度
     * @param blue 蓝色分量 (0-255) - 蓝色通道的强度
     * @param alpha 透明度分量 (0-255) - 透明度通道的强度，255为完全不透明，0为完全透明
     *
     * Sets shader color with RGB values between 0 and 255.
     * This is a convenience method that allows specifying colors using the more common 0-255 integer range.
     * Internally, these values are converted to the 0.0-1.0 float range.
     *
     * @param red Red component (0-255) - intensity of the red channel
     * @param green Green component (0-255) - intensity of the green channel
     * @param blue Blue component (0-255) - intensity of the blue channel
     * @param alpha Alpha component (0-255) - intensity of the transparency channel, 255 is fully opaque, 0 is fully transparent
     */
    public static void setColorRgb(int red, int green, int blue, int alpha) {
        RenderSystem.setShaderColor(
                red / 255.0F,
                green / 255.0F,
                blue / 255.0F,
                alpha / 255.0F
        );
    }

    /**
     * 从一个打包的ARGB整数(0xAARRGGBB)设置着色器颜色。
     * 这是一个便捷方法，允许使用单个32位整数来表示完整的ARGB颜色。
     * 适用于存储在整数中的颜色值，如GUI主题颜色、配置中定义的颜色等。
     *
     * @param argb 打包的ARGB颜色 - 32位整数，格式为0xAARRGGBB
     *             其中AA=alpha(透明度)，RR=红色，GG=绿色，BB=蓝色
     *
     * Sets shader color from a packed ARGB integer (0xAARRGGBB).
     * This is a convenience method that allows specifying a complete ARGB color as a single 32-bit integer.
     * Useful for color values stored as integers, such as GUI theme colors, colors defined in configuration, etc.
     *
     * @param argb Packed ARGB color - 32-bit integer in 0xAARRGGBB format
     *             where AA=alpha(transparency), RR=red, GG=green, BB=blue
     */
    public static void setColorARGB(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0F;
        float r = ((argb >> 16) & 0xFF) / 255.0F;
        float g = ((argb >> 8) & 0xFF) / 255.0F;
        float b = (argb & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, a);
    }

    /**
     * 从一个打包的RGB整数(0xRRGGBB)和给定的alpha值设置着色器颜色。
     * 这个方法适用于RGB颜色常量，同时需要单独控制透明度的情况。
     * 例如，使用固定的颜色值，但需要动态改变透明度来实现淡入淡出效果。
     *
     * @param rgb 打包的RGB颜色 - 24位整数，格式为0xRRGGBB
     *            其中RR=红色，GG=绿色，BB=蓝色
     * @param alpha Alpha值 (0.0-1.0) - 透明度值，1.0为完全不透明，0.0为完全透明
     *
     * Sets shader color from a packed RGB integer (0xRRGGBB) with given alpha.
     * This method is useful for RGB color constants where transparency needs to be controlled separately.
     * For example, using a fixed color value but dynamically changing the transparency for fade-in/fade-out effects.
     *
     * @param rgb Packed RGB color - 24-bit integer in 0xRRGGBB format
     *            where RR=red, GG=green, BB=blue
     * @param alpha Alpha value (0.0-1.0) - transparency value, 1.0 is fully opaque, 0.0 is fully transparent
     */
    public static void setColorRGB(int rgb, float alpha) {
        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, alpha);
    }

    /**
     * 将着色器颜色重置为白色，不透明。
     * 这是恢复默认渲染颜色的便捷方法，确保之前设置的颜色不会影响后续渲染。
     * 在渲染有颜色的元素后应该调用此方法，以确保下一个渲染操作不受影响。
     *
     * Resets shader color to white with full opacity.
     * This is a convenience method to restore the default rendering color, ensuring previously set colors don't affect subsequent rendering.
     * Should be called after rendering colored elements to ensure the next rendering operation is not affected.
     */
    public static void resetColor() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * 设置雾参数。
     * 雾效是一种使远处物体逐渐融入背景颜色的技术，用于模拟大气效果并增加场景深度感。
     * 它还可以隐藏视距边缘的突然"消失"效果，使场景边界看起来更自然。
     *
     * @param startDistance 雾开始的距离 - 从此距离开始应用雾效
     * @param endDistance 雾完全不透明的距离 - 在此距离处物体完全被雾遮蔽
     * @param red 雾的红色分量 (0.0-1.0) - 雾颜色的红色成分
     * @param green 雾的绿色分量 (0.0-1.0) - 雾颜色的绿色成分
     * @param blue 雾的蓝色分量 (0.0-1.0) - 雾颜色的蓝色成分
     * @param shape 雾形状（球形、圆柱形）- 控制雾效如何在3D空间中分布
     *              球形雾在所有方向均匀分布，圆柱形雾在水平方向均匀但垂直方向不变
     *
     * Sets up fog parameters.
     * Fog is a technique that gradually blends distant objects into the background color, used to simulate atmospheric effects and increase scene depth.
     * It also helps hide the sudden "pop-in" effect at the edge of the view distance, making scene boundaries appear more natural.
     *
     * @param startDistance Distance at which fog starts - fog effect begins to apply from this distance
     * @param endDistance Distance at which fog is fully opaque - objects are completely obscured by fog at this distance
     * @param red Fog red component (0.0-1.0) - red component of the fog color
     * @param green Fog green component (0.0-1.0) - green component of the fog color
     * @param blue Fog blue component (0.0-1.0) - blue component of the fog color
     * @param shape Fog shape (sphere, cylinder) - controls how the fog is distributed in 3D space
     *              Spherical fog is uniform in all directions, cylindrical fog is uniform horizontally but constant vertically
     */
    public static void setupFog(float startDistance, float endDistance, float red, float green, float blue, FogShape shape) {
        RenderSystem.setShaderFogStart(startDistance);
        RenderSystem.setShaderFogEnd(endDistance);
        RenderSystem.setShaderFogColor(red, green, blue);
        RenderSystem.setShaderFogShape(shape);
    }

    /**
     * 为着色器设置光照方向。
     * 在3D渲染中，光照方向决定了物体表面的明暗变化，对于创建逼真的场景至关重要。
     * 这些方向向量用于计算漫反射和镜面反射等光照效果。
     *
     * @param lightVector0 第一个光照方向 - 主光源的方向向量（通常代表太阳/月亮）
     * @param lightVector1 第二个光照方向 - 次要光源的方向向量（通常代表环境光或天空光）
     *
     * Sets up lighting directions for shaders.
     * In 3D rendering, lighting directions determine how light and shadow fall on object surfaces, crucial for creating realistic scenes.
     * These direction vectors are used to calculate lighting effects such as diffuse and specular reflections.
     *
     * @param lightVector0 First light direction - direction vector of the primary light source (typically represents sun/moon)
     * @param lightVector1 Second light direction - direction vector of the secondary light source (typically represents ambient or sky light)
     */
    public static void setupLighting(Vector3f lightVector0, Vector3f lightVector1) {
        RenderSystem.setShaderLights(lightVector0, lightVector1);
    }

    /**
     * 使用指定的比较函数设置深度测试。
     * 深度测试是3D渲染中的关键技术，用于确定哪些像素应该被绘制，哪些应该被遮挡。
     * 它通过比较新像素与已存在像素的深度值（与摄像机的距离）来工作：
     * - 如果新像素通过深度测试（例如，它比现有像素更接近摄像机），则会被绘制
     * - 如果新像素未通过深度测试，则会被丢弃，保留已有像素
     *
     * @param depthFunc 深度比较函数 - 确定如何比较深度值
     *                  常见值包括：
     *                  - GL11.GL_LESS：当新像素深度小于当前深度时绘制（标准设置，近处物体遮挡远处物体）
     *                  - GL11.GL_LEQUAL：当新像素深度小于或等于当前深度时绘制
     *                  - GL11.GL_GREATER：当新像素深度大于当前深度时绘制（反向深度测试，用于特殊效果）
     *                  - GL11.GL_ALWAYS：总是通过深度测试（通常用于2D元素或特殊效果）
     *
     * Sets up the depth test with the specified comparison function.
     * Depth testing is a critical technique in 3D rendering that determines which pixels should be drawn and which should be occluded.
     * It works by comparing the depth value (distance from camera) of a new pixel with the existing pixel:
     * - If the new pixel passes the depth test (e.g., it's closer to the camera than the existing one), it gets drawn
     * - If the new pixel fails the depth test, it's discarded, preserving the existing pixel
     *
     * @param depthFunc Depth comparison function - determines how depth values are compared
     *                  Common values include:
     *                  - GL11.GL_LESS: Draw when new depth is less than current depth (standard setting, near objects occlude far objects)
     *                  - GL11.GL_LEQUAL: Draw when new depth is less than or equal to current depth
     *                  - GL11.GL_GREATER: Draw when new depth is greater than current depth (reverse depth test, for special effects)
     *                  - GL11.GL_ALWAYS: Always pass depth test (typically used for 2D elements or special effects)
     */
    public static void setupDepthTest(int depthFunc) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(depthFunc);
    }

    /**
     * 禁用深度测试。
     * 当深度测试被禁用时，像素会被绘制而不考虑它们的深度值。
     * 适用于2D UI元素、特殊效果或需要独立于深度绘制的情况。
     * 注意：禁用深度测试可能导致渲染顺序问题，应谨慎使用。
     *
     * Disables depth test.
     * When depth testing is disabled, pixels are drawn regardless of their depth value.
     * Useful for 2D UI elements, special effects, or cases where drawing needs to happen independently of depth.
     * Note: Disabling depth testing can lead to rendering order issues and should be used with caution.
     */
    public static void disableDepthTest() {
        RenderSystem.disableDepthTest();
    }

    /**
     * 启用深度写入。
     * 深度写入控制是否更新深度缓冲区。启用后，绘制的像素会更新深度缓冲区。
     * 这是深度测试的正常操作模式：绘制像素时同时更新深度值。
     *
     * Enables depth writing.
     * Depth writing controls whether the depth buffer gets updated. When enabled, drawn pixels update the depth buffer.
     * This is the normal mode of operation for depth testing: updating the depth value when drawing pixels.
     */
    public static void enableDepthWrite() {
        RenderSystem.depthMask(true);
    }

    /**
     * 禁用深度写入。
     * 当深度写入被禁用时，绘制的像素不会更新深度缓冲区。
     * 这对于渲染透明物体很有用：先绘制不透明物体并更新深度，
     * 然后绘制透明物体而不更新深度，以确保正确的透明效果。
     *
     * Disables depth writing.
     * When depth writing is disabled, drawn pixels don't update the depth buffer.
     * This is useful for rendering transparent objects: first draw opaque objects and update depth,
     * then draw transparent objects without updating depth to ensure correct transparency effects.
     */
    public static void disableDepthWrite() {
        RenderSystem.depthMask(false);
    }

    /**
     * 使用指定宽度设置线条渲染。
     * 控制线条的粗细，用于绘制线框、轮廓、调试可视化等。
     * 注意：在现代OpenGL中，线宽支持可能受到限制，某些硬件可能仅支持宽度为1.0的线条。
     *
     * @param width 线条宽度（像素）- 线条的粗细，以像素为单位
     *
     * Sets up line rendering with specified width.
     * Controls the thickness of lines, used for drawing wireframes, outlines, debug visualizations, etc.
     * Note: In modern OpenGL, line width support may be limited, and some hardware may only support a width of 1.0.
     *
     * @param width Line width in pixels - thickness of the line in pixels
     */
    public static void setupLineRendering(float width) {
        RenderSystem.lineWidth(width);
        // In modern OpenGL, you might need to use a shader for proper line rendering
    }

    /**
     * 设置线框渲染模式。
     * 在线框模式下，多边形仅绘制其边缘，而不填充内部。
     * 这对于调试几何体、查看网格结构或创建特殊视觉效果很有用。
     *
     * Sets up wireframe rendering mode.
     * In wireframe mode, polygons are drawn only as their edges, without filling the interior.
     * This is useful for debugging geometry, examining mesh structure, or creating special visual effects.
     */
    public static void setupWireframeMode() {
        RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    }

    /**
     * 设置填充渲染模式（默认）。
     * 在填充模式下，多边形会填充其内部，这是3D图形的标准渲染模式。
     * 应在使用线框模式后调用此方法，以恢复正常的实心渲染。
     *
     * Sets up filled rendering mode (default).
     * In fill mode, polygons are filled in their interior, which is the standard rendering mode for 3D graphics.
     * Should be called after using wireframe mode to restore normal solid rendering.
     */
    public static void setupFillMode() {
        RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }
    /**
     * 从姿态堆栈更新模型视图矩阵。
     * 模型视图矩阵是3D渲染中的核心变换矩阵，它结合了模型变换（物体的位置、旋转和缩放）
     * 和视图变换（摄像机的位置和朝向）。此方法将当前姿态堆栈的变换应用到渲染管线，
     * 确保物体在正确的位置和方向上被渲染。
     *
     * Updates model view matrix from pose stack.
     * The model view matrix is a core transformation matrix in 3D rendering that combines the model transformation
     * (object's position, rotation, and scale) and the view transformation (camera position and orientation).
     * This method applies the current pose stack transformations to the rendering pipeline,
     * ensuring objects are rendered at the correct position and orientation.
     */
    public static void applyModelView() {
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * 设置视口尺寸。
     * 视口定义了渲染输出的矩形区域，所有渲染操作的结果都将显示在此区域内。
     * 视口变换是将规范化设备坐标(NDC)转换为窗口（屏幕）坐标的最后一步。
     * 修改视口可以实现分屏渲染、小地图、画中画等效果。
     *
     * @param x 视口的X坐标 - 视口左下角在窗口中的水平位置（以像素为单位）
     * @param y 视口的Y坐标 - 视口左下角在窗口中的垂直位置（以像素为单位）
     * @param width 视口宽度 - 视口的水平尺寸（以像素为单位）
     * @param height 视口高度 - 视口的垂直尺寸（以像素为单位）
     *
     * Sets viewport dimensions.
     * The viewport defines the rectangular area where rendering output is displayed, and all rendering operations
     * will be shown within this area. The viewport transformation is the final step that converts normalized device
     * coordinates (NDC) to window (screen) coordinates.
     * Modifying the viewport allows for effects like split-screen rendering, minimaps, picture-in-picture, etc.
     *
     * @param x X coordinate of the viewport - horizontal position of the viewport's lower-left corner in the window (in pixels)
     * @param y Y coordinate of the viewport - vertical position of the viewport's lower-left corner in the window (in pixels)
     * @param width Width of the viewport - horizontal dimension of the viewport (in pixels)
     * @param height Height of the viewport - vertical dimension of the viewport (in pixels)
     */
    public static void setViewport(int x, int y, int width, int height) {
        RenderSystem.viewport(x, y, width, height);
    }

    /**
     * 配置剪切测试（剪切矩形）。
     * 剪切测试是一种限制渲染区域的技术，只有在指定矩形区域内的像素才会被渲染，
     * 区域外的所有像素都会被丢弃。这与视口的区别在于，视口定义了整个渲染目标区域，
     * 而剪切测试是在该区域内进一步限制渲染的一种方式。
     * 常用于UI元素（如滚动面板）、特效（如通过传送门的视图）等。
     *
     * @param x 剪切框的X坐标 - 剪切区域左下角的水平位置
     * @param y 剪切框的Y坐标 - 剪切区域左下角的垂直位置
     * @param width 剪切框宽度 - 剪切区域的水平尺寸
     * @param height 剪切框高度 - 剪切区域的垂直尺寸
     *
     * Configures scissor test (clipping rectangle).
     * Scissor testing is a technique to restrict rendering to a specific rectangular area, where only pixels
     * within the specified rectangle will be rendered and all pixels outside will be discarded.
     * This differs from the viewport in that the viewport defines the entire render target area,
     * while scissoring is a way to further restrict rendering within that area.
     * Commonly used for UI elements (like scrolling panels), effects (like views through portals), etc.
     *
     * @param x X coordinate of the scissor box - horizontal position of the lower-left corner of the scissor region
     * @param y Y coordinate of the scissor box - vertical position of the lower-left corner of the scissor region
     * @param width Width of the scissor box - horizontal dimension of the scissor region
     * @param height Height of the scissor box - vertical dimension of the scissor region
     */
    public static void enableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }

    /**
     * 禁用剪切测试。
     * 在启用剪切测试后，需要调用此方法来关闭剪切测试，以便恢复到正常的全屏渲染模式。
     * 如果不禁用剪切测试，后续的所有渲染操作都会继续受到剪切区域的限制。
     *
     * Disables scissor test.
     * After enabling scissor testing, this method should be called to turn off scissor testing and
     * restore normal full-screen rendering mode. If scissor testing is not disabled,
     * all subsequent rendering operations will continue to be constrained by the scissor region.
     */
    public static void disableScissor() {
        RenderSystem.disableScissor();
    }

    /**
     * 设置帧缓冲的清除颜色。
     * 帧缓冲清除颜色是在每一帧开始时用来填充整个渲染目标的颜色。这通常是场景的"背景色"。
     * 设置清除颜色不会立即清除缓冲区，而是定义了在调用clearColorAndDepth()等方法时使用的颜色。
     * 这对于创建渐变天空、不同环境（如水下、虚空等）的氛围非常重要。
     *
     * @param red 红色分量 (0.0-1.0) - 背景色的红色强度
     * @param green 绿色分量 (0.0-1.0) - 背景色的绿色强度
     * @param blue 蓝色分量 (0.0-1.0) - 背景色的蓝色强度
     * @param alpha 透明度分量 (0.0-1.0) - 背景色的透明度，通常为1.0（完全不透明）
     *
     * Sets clear color for the frame buffer.
     * The frame buffer clear color is the color used to fill the entire render target at the beginning of each frame.
     * This is typically the "background color" of the scene. Setting the clear color does not immediately clear the buffer,
     * but defines the color to be used when methods like clearColorAndDepth() are called.
     * This is important for creating gradient skies, different environments (like underwater, void, etc.).
     *
     * @param red Red component (0.0-1.0) - intensity of red in the background color
     * @param green Green component (0.0-1.0) - intensity of green in the background color
     * @param blue Blue component (0.0-1.0) - intensity of blue in the background color
     * @param alpha Alpha component (0.0-1.0) - transparency of the background color, typically 1.0 (fully opaque)
     */
    public static void setClearColor(float red, float green, float blue, float alpha) {
        RenderSystem.clearColor(red, green, blue, alpha);
    }

    /**
     * 清除颜色和深度缓冲。
     * 此方法将帧缓冲区的颜色重置为之前通过setClearColor()设置的颜色，
     * 并将深度缓冲区重置为最大深度值（通常为1.0）。这通常在每一帧的开始调用，
     * 以清除前一帧的渲染结果，准备新的一帧渲染。
     * 如果不清除这些缓冲区，新的渲染内容将与前一帧的内容混合，通常会导致不希望的视觉效果。
     *
     * Clears color and depth buffers.
     * This method resets the frame buffer color to the color previously set with setClearColor(),
     * and resets the depth buffer to the maximum depth value (typically 1.0). This is usually called
     * at the beginning of each frame to clear the previous frame's rendering results and prepare for new rendering.
     * If these buffers are not cleared, new rendering action will blend with the previous frame's action,
     * typically causing undesired visual effects.
     */
    public static void clearColorAndDepth() {
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, true);
    }

    /**
     * 确定当前线程是否为渲染线程。
     * 在多线程环境中，OpenGL调用必须在专门的渲染线程上执行。这个方法可以用来检查
     * 当前代码是否在正确的线程上执行。如果在非渲染线程上尝试执行渲染操作，可能会导致
     * 不可预测的行为、渲染错误或崩溃。
     *
     * @return 如果在渲染线程上则为true，否则为false - 表示当前线程是否为专用的OpenGL渲染线程
     *
     * Determines if the current thread is the render thread.
     * In a multi-threaded environment, OpenGL calls must be executed on a dedicated render thread.
     * This method can be used to check if the current code is executing on the correct thread.
     * Attempting to perform rendering operations on non-render threads may lead to unpredictable
     * behavior, rendering errors, or crashes.
     *
     * @return true if on render thread, false otherwise - indicates whether the current thread is the dedicated OpenGL rendering thread
     */
    public static boolean isOnRenderThread() {
        return RenderSystem.isOnRenderThread();
    }

    /**
     * 确定当前线程是否为游戏线程。
     * 游戏线程是主线程，负责游戏逻辑、输入处理和更新循环。某些操作（如更新游戏状态）
     * 必须在游戏线程上执行，而不是在渲染线程上。此方法可用于确保代码在正确的线程上执行。
     *
     * @return 如果在游戏线程上则为true，否则为false - 表示当前线程是否为主游戏逻辑线程
     *
     * Determines if the current thread is the game thread.
     * The game thread is the main thread responsible for game logic, input processing, and update loops.
     * Certain operations (like updating game state) must be performed on the game thread rather than
     * the render thread. This method can be used to ensure code is executing on the correct thread.
     *
     * @return true if on game thread, false otherwise - indicates whether the current thread is the main game logic thread
     */
    public static boolean isOnGameThread() {
        return RenderSystem.isOnGameThread();
    }

    /**
     * 获取最大支持的纹理尺寸。
     * 不同的GPU和驱动程序支持不同的最大纹理尺寸。此方法返回当前硬件支持的最大纹理维度。
     * 尝试创建大于此尺寸的纹理可能会失败或导致性能问题。创建或加载纹理之前，应检查此值
     * 以确保兼容性，特别是对于高分辨率纹理资源。
     *
     * @return 最大纹理尺寸（像素）- 当前GPU支持的最大纹理宽度/高度
     *
     * Gets the maximum supported texture size.
     * Different GPUs and drivers support different maximum texture sizes. This method returns the maximum
     * texture dimension supported by the current hardware. Attempting to create textures larger than this
     * size may fail or cause performance issues. This value should be checked before creating or loading
     * textures to ensure compatibility, especially for high-resolution texture assets.
     *
     * @return Maximum texture size in pixels - maximum texture width/height supported by the current GPU
     */
    public static int getMaxTextureSize() {
        return RenderSystem.maxSupportedTextureSize();
    }

    /**
     * 将Vector3f转换为归一化的Vector3f。
     * 归一化向量是长度（模）为1的向量，保持原始向量的方向但标准化其长度。
     * 在图形渲染中，归一化向量对于表示方向（如法线、光照方向等）非常重要，
     * 因为它们允许在不考虑距离的情况下进行一致的方向计算。
     *
     * @param vector 要归一化的向量 - 输入向量，可以是任何长度
     * @return 新的归一化向量 - 与输入向量方向相同但长度为1的新向量
     *
     * Converts a Vector3f to a normalized Vector3f.
     * A normalized vector is a vector with length (magnitude) of 1, preserving the original vector's direction
     * but standardizing its length. In graphics rendering, normalized vectors are important for representing
     * directions (such as normals, light directions, etc.) as they allow for consistent directional calculations
     * without regard to distance.
     *
     * @param vector The vector to normalize - input vector of any length
     * @return A new normalized vector - a new vector with the same direction as the input but with a length of 1
     */
    public static Vector3f normalize(Vector3f vector) {
        Vector3f result = new Vector3f(vector);
        result.normalize();
        return result;
    }

    /**
     * 从Vec3创建Vector3f。
     * 这是一个在Minecraft的Vec3（双精度）和JOML的Vector3f（单精度）之间进行转换的实用方法。
     * 在渲染代码中，通常需要将Minecraft的世界坐标（使用Vec3）转换为渲染系统使用的格式（Vector3f）。
     * 请注意，这会导致精度从双精度降低到单精度，但对于大多数渲染目的来说已足够。
     *
     * @param vec 要转换的Vec3 - 包含双精度坐标的源向量
     * @return 新的Vector3f - 包含相同坐标的单精度向量
     *
     * Creates a Vector3f from a Vec3.
     * This is a utility method for conversion between Minecraft's Vec3 (double precision) and JOML's Vector3f (single precision).
     * In rendering code, it's often necessary to convert from Minecraft's world coordinates (using Vec3) to
     * the format used by the rendering system (Vector3f). Note that this results in a reduction of precision
     * from double to single precision, but this is sufficient for most rendering purposes.
     *
     * @param vec The Vec3 to convert - source vector containing double-precision coordinates
     * @return A new Vector3f - single-precision vector containing the same coordinates
     */
    public static Vector3f toVector3f(Vec3 vec) {
        return new Vector3f((float)vec.x, (float)vec.y, (float)vec.z);
    }

    /**
     * 从RGB值（0-255）和alpha值（0.0-1.0）创建Vector4f。
     * 这个方法提供了一种便捷的方式，可以使用常见的RGB整数值（0-255范围）和浮点alpha值
     * 创建颜色向量。返回的向量使用归一化的颜色分量（0.0-1.0范围），适用于着色器输入。
     * 这在处理用户界面、粒子效果或任何需要颜色参数的渲染时非常有用。
     *
     * @param r 红色分量 (0-255) - 颜色的红色强度，使用传统的8位整数范围
     * @param g 绿色分量 (0-255) - 颜色的绿色强度，使用传统的8位整数范围
     * @param b 蓝色分量 (0-255) - 颜色的蓝色强度，使用传统的8位整数范围
     * @param a 透明度分量 (0.0-1.0) - 透明度值，1.0为完全不透明，0.0为完全透明
     * @return 值在0.0-1.0范围内的新Vector4f - 包含归一化颜色分量的向量
     *
     * Creates a Vector4f from RGB values (0-255) and alpha (0.0-1.0).
     * This method provides a convenient way to create a color vector using common RGB integer values
     * (in the 0-255 range) and a floating-point alpha value. The returned vector uses normalized color
     * components (in the 0.0-1.0 range) suitable for shader inputs.
     * This is useful when working with user interfaces, particle effects, or any rendering that requires color parameters.
     *
     * @param r Red component (0-255) - red intensity of the color using the traditional 8-bit integer range
     * @param g Green component (0-255) - green intensity of the color using the traditional 8-bit integer range
     * @param b Blue component (0-255) - blue intensity of the color using the traditional 8-bit integer range
     * @param a Alpha component (0.0-1.0) - transparency value, 1.0 is fully opaque, 0.0 is fully transparent
     * @return A new Vector4f with values normalized to 0.0-1.0 - vector containing normalized color components
     */
    public static Vector4f createColorVec4f(int r, int g, int b, float a) {
        return new Vector4f(r / 255.0F, g / 255.0F, b / 255.0F, a);
    }

    /**
     * 获取当前着色器实例。
     * 这个方法返回当前绑定到渲染管线的着色器程序。着色器是在GPU上运行的程序，
     * 控制着顶点处理和像素着色。获取当前着色器可以用于检查状态、修改特定着色器的参数，
     * 或确保在进行依赖于特定着色器的渲染操作之前已设置了正确的着色器。
     *
     * @return 当前着色器实例，如果没有绑定则为null - 当前活动的着色器程序或null
     *
     * Gets the current shader instance.
     * This method returns the shader program currently bound to the rendering pipeline. Shaders are programs
     * that run on the GPU and control vertex processing and pixel shading. Getting the current shader
     * can be useful for checking state, modifying parameters specific to a shader, or ensuring the
     * correct shader is set before performing rendering operations that depend on specific shaders.
     *
     * @return Current shader instance or null if none is bound - the currently active shader program or null
     */
    public static ShaderInstance getCurrentShader() {
        return RenderSystem.getShader();
    }

    /**
     * 获取绑定到指定着色器纹理槽的纹理ID。
     * 纹理ID是OpenGL分配给纹理资源的唯一标识符。此方法返回当前绑定到特定纹理槽的纹理ID。
     * 这可用于检查纹理绑定状态、验证正确的纹理已设置，或在高级渲染技术中处理多个纹理。
     * 纹理槽是允许同时使用多个纹理的索引位置（OpenGL通常支持至少16个纹理单元）。
     *
     * @param slot 纹理槽索引 (0-11) - 要查询的纹理单元槽位
     * @return 纹理ID，如果没有绑定纹理则为0 - 绑定到指定槽的纹理的OpenGL句柄
     *
     * Gets the texture ID bound to the specified shader texture slot.
     * Texture IDs are unique identifiers assigned by OpenGL to texture resources. This method returns the
     * texture ID currently bound to a specific texture slot. This can be used to check texture binding states,
     * verify that the correct textures are set, or work with multiple textures in advanced rendering techniques.
     * Texture slots are indexed positions that allow multiple textures to be used simultaneously
     * (OpenGL typically supports at least 16 texture units).
     *
     * @param slot Texture slot index (0-11) - the texture unit slot to query
     * @return Texture ID or 0 if no texture is bound - the OpenGL handle of the texture bound to the specified slot
     */
    public static int getTextureId(int slot) {
        if (slot < 0 || slot > 11) {
            return 0;
        }
        return RenderSystem.getShaderTexture(slot);
    }
}