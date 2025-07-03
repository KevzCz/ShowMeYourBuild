package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
@Environment(EnvType.CLIENT)
public abstract class StatEntry {
    public int column = 0;
    public abstract void render(DrawContext context, TextRenderer tr, int x, int y);
    public abstract void renderTooltip(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY, int x, int y, float scrollY);
}
