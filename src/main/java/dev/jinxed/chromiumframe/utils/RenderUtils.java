package dev.jinxed.chromiumframe.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderUtils {
    public static void startRender() {
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void endRender() {
        RenderSystem.enableCull();
        RenderSystem.depthFunc(GL11.GL_EQUAL);
    }
}
