package dev.jinxed.chromiumframe.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.jinxed.chromiumframe.utils.AlphaOverride;
import dev.jinxed.chromiumframe.utils.BufferUtils;
import dev.jinxed.chromiumframe.utils.RenderUtils;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.Range;
import org.joml.Math;
import org.joml.Matrix4f;

import java.awt.*;

public class TwoDimensionRenderer {
    private static final float[][] roundedCache = new float[][] { new float[3], new float[3], new float[3], new float[3], };

    static float transformColor(float f) {
        return AlphaOverride.compute(f);
    }

    static float[] getColor(Color c) {
        return new float[] { c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, transformColor(c.getAlpha() / 255f) };
    }

    public static void renderCircle(PoseStack stack, Color color, double originX, double originY, double rad, @Range(from = 4, to = 360) int segments) {
        //segments = MathHelper.clamp(segments, 4, 360);

        Matrix4f matrix = stack.last().pose();
        float[] colorFloat = getColor(color);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 360; i += Math.min((360d / segments), 360 - i)) {
            double radians = Math.toRadians(i);
            double sin = Math.sin(radians) * rad;
            double cos = Math.cos(radians) * rad;
            buffer.vertex(matrix, (float) (originX + sin), (float) (originY + cos), 0).color(colorFloat[0], colorFloat[1], colorFloat[2], colorFloat[3]).endVertex();
        }
        RenderUtils.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUtils.draw(buffer);
        RenderUtils.endRender();
    }

    /**
     * Renders a rounded rectangle
     *
     * @param stack   The context PoseStack
     * @param color   The color of the rectangle
     * @param x       The start X coordinate
     * @param y       The start Y coordinate
     * @param x1      The end X coordinate
     * @param y1      The end Y coordinate
     */
    public static void renderQuad(PoseStack stack, Color color, int x, int y, int x1, int y1) {
        int j;
        if (x < x1) {
            j = x;
            x = x1;
            x1 = j;
        }

        if (y < y1) {
            j = y;
            y = y1;
            y1 = j;
        }

        Matrix4f matrix = stack.last().pose();
        float[] colors = getColor(color);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, (float) x, (float) y1, 0.0F).color(colors[0], colors[1], colors[2], colors[3]).endVertex();
        buffer.vertex(matrix, (float) x1, (float) y1, 0.0F).color(colors[0], colors[1], colors[2], colors[3]).endVertex();
        buffer.vertex(matrix, (float) x1, (float) y, 0.0F).color(colors[0], colors[1], colors[2], colors[3]).endVertex();
        buffer.vertex(matrix, (float) x, (float) y, 0.0F).color(colors[0], colors[1], colors[2], colors[3]).endVertex();

        RenderUtils.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator.getInstance().end();
        RenderUtils.endRender();
    }

    private static void _populateRC(float a, float b, float c, int i) {
        roundedCache[i][0] = a;
        roundedCache[i][1] = b;
        roundedCache[i][2] = c;
    }

    private static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, float fromX, float fromY, float toX, float toY, float radC1, float radC2, float radC3, float radC4, float samples) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        _populateRC(toX - radC4, toY - radC4, radC4, 0);
        _populateRC(toX - radC2, fromY + radC2, radC2, 1);
        _populateRC(fromX + radC1, fromY + radC1, radC1, 2);
        _populateRC(fromX + radC3, toY - radC3, radC3, 3);
        for (int i = 0; i < 4; i++) {
            float[] current = roundedCache[i];
            float rad = current[2];
            for (float r = i * 90f; r <= (i + 1) * 90f; r += (90 / samples)) {
                float rad1 = Math.toRadians(r);
                float sin = Math.sin(rad1) * rad;
                float cos = Math.cos(rad1) * rad;

                bufferBuilder.vertex(matrix, current[0] + sin, current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
            }
        }
        Tesselator.getInstance().end();
    }

    /**
     * Renders a rounded rectangle
     *
     * @param stack   The context PoseStack
     * @param color   The color of the rectangle
     * @param x       The start X coordinate
     * @param y       The start Y coordinate
     * @param x1      The end X coordinate
     * @param y1      The end Y coordinate
     * @param radTL   The radius of the top left corner
     * @param radTR   The radius of the top right corner
     * @param radBL   The radius of the bottom left corner
     * @param radBR   The radius of the bottom right corner
     * @param samples The amount of samples to use for the corners
     */
    public static void renderRoundedQuad(PoseStack stack, Color color, double x, double y, double x1, double y1, float radTL, float radTR, float radBL, float radBR, float samples) {
        Matrix4f matrix = stack.last().pose();
        float[] color1 = getColor(color);
        float r = color1[0];
        float g = color1[1];
        float b = color1[2];
        float a = color1[3];

        RenderUtils.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrix, r, g, b, a, (float) x, (float) y, (float) x1, (float) y1, radTL, radTR, radBL, radBR, samples);
        RenderUtils.endRender();
    }

    /**
     * Renders a rounded rectangle
     *
     * @param stack   The context PoseStack
     * @param c       The color of the rectangle
     * @param x       The start X coordinate
     * @param y       The start Y coordinate
     * @param x1      The end X coordinate
     * @param y1      The end Y coordinate
     * @param rad     Radius of the corners
     * @param samples The amount of samples per corner
     */
    public static void renderRoundedQuad(PoseStack stack, Color c, double x, double y, double x1, double y1, float rad, float samples) {
        renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples);
    }

    /**
     * Renders a regular line between 2 points
     *
     * @param stack The context PoseStack
     * @param color The color of the line
     * @param x     The start X coordinate
     * @param y     The start Y coordinate
     * @param x1    The end X coordinate
     * @param y1    The end Y coordinate
     */
    public static void renderLine(PoseStack stack, Color color, double x, double y, double x1, double y1) {
        float[] colorFloat = getColor(color);
        Matrix4f m = stack.last().pose();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(m, (float) x, (float) y, 0f).color(colorFloat[0], colorFloat[1], colorFloat[2], colorFloat[3]).endVertex();
        bufferBuilder.vertex(m, (float) x1, (float) y1, 0f).color(colorFloat[0], colorFloat[1], colorFloat[2], colorFloat[3]).endVertex();

        RenderUtils.startRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator.getInstance().end();
        RenderUtils.endRender();
    }
}
