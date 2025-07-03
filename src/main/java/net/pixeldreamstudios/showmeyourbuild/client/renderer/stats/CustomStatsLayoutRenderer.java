package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomStatsLayoutRenderer {

    private static final Identifier HEART = Identifier.of("minecraft", "textures/particle/heart.png");
    private static final Identifier ARMOR = Identifier.of("showmeyourbuild", "textures/gui/armor.png");
    private static final Identifier ARMOR_TOUGHNESS = Identifier.of("showmeyourbuild", "textures/gui/toughness.png");
    private static final Identifier SPEED = Identifier.of("minecraft", "textures/mob_effect/speed.png");
    private static final Identifier LUCK = Identifier.of("minecraft", "textures/mob_effect/luck.png");
    private static final Identifier ATTACK_DAMAGE = Identifier.of("showmeyourbuild", "textures/gui/axe.png");
    private static final Identifier ATTACK_SPEED = Identifier.of("minecraft", "textures/gui/sprites/hud/hotbar_attack_indicator_progress.png");

    public static void render(DrawContext ctx, TextRenderer tr, int x, int y, int mouseX, int mouseY, NbtCompound attrs) {
        int spacing = 60;
        int textPadding = 5;
        int rowHeight = 14;

        // Row 1
        Identifier[] icons1 = {HEART, ARMOR, ARMOR_TOUGHNESS, SPEED};
        String[] values1 = {
                getAttrFinal(attrs, "minecraft:generic.max_health"),
                getAttrFinal(attrs, "minecraft:generic.armor"),
                getAttrFinal(attrs, "minecraft:generic.armor_toughness"),
                getAttrFinal(attrs, "minecraft:generic.movement_speed")
        };
        String[] tips1 = {
                "Max Health", "Armor", "Armor Toughness", "Movement Speed"
        };

        for (int i = 0; i < icons1.length; i++) {
            int iconX = x + spacing * i;
            int iconY = y;

            ctx.drawTexture(icons1[i], iconX, iconY, 0, 0, 10, 10, 10, 10);
            ctx.drawTextWithShadow(tr, values1[i], iconX + 14, iconY + 2, 0xAAAAAA);
            if (mouseX >= iconX && mouseX < iconX + 10 && mouseY >= iconY && mouseY < iconY + 10) {
                ctx.drawTooltip(tr, Text.literal(tips1[i]), mouseX, mouseY);
            }
        }

        // Row 2
        Identifier[] icons2 = {ATTACK_DAMAGE, ATTACK_SPEED, LUCK};
        String[] values2 = {
                getAttrFinal(attrs, "minecraft:generic.attack_damage"),
                getAttrFinal(attrs, "minecraft:generic.attack_speed"),
                getAttrFinal(attrs, "minecraft:generic.luck")
        };
        String[] tips2 = {
                "Attack Damage", "Attack Speed", "Luck"
        };

        int row2BaseX = x + (spacing * 4 - spacing * 3) / 2; // Center 3 icons under 4

        int iconY = y + rowHeight;
        for (int i = 0; i < icons2.length; i++) {
            int iconX = row2BaseX + spacing * i;
            ctx.drawTexture(icons2[i], iconX, iconY, 0, 0, 10, 10, 10, 10);
            ctx.drawTextWithShadow(tr, values2[i], iconX + 14, iconY + 2, 0xAAAAAA);
            if (mouseX >= iconX && mouseX < iconX + 10 && mouseY >= iconY && mouseY < iconY + 10) {
                ctx.drawTooltip(tr, Text.literal(tips2[i]), mouseX, mouseY);
            }
        }

        // Divider
        ctx.fill(x, iconY + 14, x + 215, iconY + 15, 0xFFAAAAAA);
    }

    private static String getAttrFinal(NbtCompound attrs, String key) {
        if (!attrs.contains(key)) return "-";
        NbtCompound attr = attrs.getCompound(key);
        if (!attr.contains("Final")) return "-";
        double val = attr.getDouble("Final");
        if (Double.isNaN(val)) return "-";
        return String.format("%.2f", val).replaceAll("\\.00$", "");
    }
}
