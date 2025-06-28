package net.pixeldreamstudios.showmeyourbuild.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.data.ClientSkillScreenData;
import net.puffish.skillsmod.client.gui.SkillsScreen;

import java.util.List;
import java.util.Optional;

public class ReadOnlySkillsScreen extends SkillsScreen {
    public ReadOnlySkillsScreen(ClientSkillScreenData data, Optional<Identifier> categoryId) {
        super(data, categoryId);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    public static void open(List<ClientCategoryData> categories, Optional<Identifier> categoryId) {
        // Build data object
        ClientSkillScreenData data = new ClientSkillScreenData();
        for (ClientCategoryData category : categories) {
            data.putCategory(category.getConfig().id(), category);
        }
        for (ClientCategoryData category : categories) {
            System.out.println(" - " + category.getConfig().id());
        }

        MinecraftClient.getInstance().setScreen(new ReadOnlySkillsScreen(data, categoryId));
    }

}
