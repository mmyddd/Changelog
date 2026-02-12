package io.github.cpearl0.ctnhchangelog.mixin;

import io.github.cpearl0.ctnhchangelog.Config;
import io.github.cpearl0.ctnhchangelog.client.ChangelogTab;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public abstract class SelectWorldScreenMixin extends Screen {
    @Shadow
    private EditBox searchBox;

    protected SelectWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;

        if (this.searchBox != null) {
            int buttonWidth = 60;
            int buttonX = this.searchBox.getX() + this.searchBox.getWidth() + 4;
            int buttonY = this.searchBox.getY();
            int buttonHeight = this.searchBox.getHeight();

            Button changelogButton = Button.builder(Component.translatable("ctnhchangelog.button.changelog"), (button) -> {
                ChangelogTab.shouldOpenChangelogTab = true;
                CreateWorldScreen.openFresh(this.minecraft, this);
            }).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build();

            this.addRenderableWidget(changelogButton);
        }
    }
}