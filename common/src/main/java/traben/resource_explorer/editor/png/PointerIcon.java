package traben.resource_explorer.editor.png;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;

import static traben.resource_explorer.ResourceExplorerClient.MOD_ID;

enum PointerIcon {
    //        NONE(){
//            @Override
//            void render(DrawContext context, int mouseX, int mouseY) {
//
//            }
//        },
    BRUSH() {
        @Override
        void render(DrawContext context, int mouseX, int mouseY, int color) {
            var y = mouseY - 16;
            var u = 0f;
            var v = 0f;
            var width = 16;
            var height = 16;
            var textureWidth = 16;
            var textureHeight = 16;

            context.drawTexture(new Identifier(MOD_ID, "textures/pointer_brush_handle.png"),
                    mouseX, y, u, v, width, height, textureWidth, textureHeight);
            drawTexturedQuad(color, context, new Identifier(MOD_ID, "textures/pointer_brush.png"),
                    mouseX, mouseX + width, y, y + height, (u + 0.0F) / (float) textureWidth, (u + (float) width) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) height) / (float) textureHeight);

        }


        void drawTexturedQuad(int color, DrawContext context, Identifier texture, int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2) {
            var r = ColorHelper.Argb.getRed(color);
            var g = ColorHelper.Argb.getGreen(color);
            var b = ColorHelper.Argb.getBlue(color);
            var a = ColorHelper.Argb.getAlpha(color);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
            RenderSystem.enableBlend();
            Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
            bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, 0).color(b, g, r, a).texture(u1, v1).next();
            bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, 0).color(b, g, r, a).texture(u1, v2).next();
            bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0).color(b, g, r, a).texture(u2, v2).next();
            bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, 0).color(b, g, r, a).texture(u2, v1).next();
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
            RenderSystem.disableBlend();
        }
    },
    HAND_MOVE() {
        @Override
        void render(DrawContext context, int mouseX, int mouseY, int color) {
            context.drawTexture(new Identifier(MOD_ID, "textures/pointer_move.png"),
                    mouseX - 8, mouseY - 8, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    },
    ERASER() {
        @Override
        void render(DrawContext context, int mouseX, int mouseY, int color) {
            context.drawTexture(new Identifier(MOD_ID, "textures/pointer_rubber.png"),
                    mouseX, mouseY - 16, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    },
    PICK() {
        @Override
        void render(DrawContext context, int mouseX, int mouseY, int color) {
            context.drawTexture(new Identifier(MOD_ID, "textures/pointer_pick.png"),
                    mouseX, mouseY - 16, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    };

    abstract void render(DrawContext context, int mouseX, int mouseY, int color);

}
