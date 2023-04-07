package dev.jinxed.chromiumframe.utils;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;

public class BufferUtils {
    /**
     * Draws a buffer
     *
     * @param builder The buffer
     */
    public static void draw(BufferBuilder builder) {
        BufferUploader.drawWithShader(builder.end());
    }
}
