package com.mmyddd.mcmod.changelog.mixin;

import com.mmyddd.mcmod.changelog.Config;
import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.ChangelogOverviewScreen;
import com.mmyddd.mcmod.changelog.client.VersionCheckService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique
    private Button ctnhChangelogButton;

    @Unique
    private boolean ctnhHasUpdate = false;

    @Unique
    private long ctnhLastBlinkTime = 0;

    @Unique
    private boolean ctnhBlinkState = true;

    @Unique
    private static final int BLINK_INTERVAL = 800; // 800ms 闪烁周期

    @Unique
    private static final int BORDER_COLOR = 0xFFFFFF00; // 黄色 (ARGB)

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onInitHead(CallbackInfo ci) {
        if (Config.isChangelogTabEnabled() && !Config.getModpackVersion().isEmpty()) {
            VersionCheckService.reset();
            VersionCheckService.checkForUpdate();
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled() || Config.getModpackVersion().isEmpty()) return;
        if (!Config.showButtonOnTitleScreen()) return;

        int l = this.height / 4 + 48;
        int buttonY = l + 72 + 12 + 24; // options按钮下方24像素

        ctnhChangelogButton = new ChangelogButton(
                this.width / 2 - 100,
                buttonY,
                200,
                20,
                Component.translatable("ctnhchangelog.button.changelog"),
                button -> {
                    ChangelogEntry.resetLoaded();
                    ChangelogEntry.loadAfterConfig();
                    Minecraft.getInstance().setScreen(
                            new ChangelogOverviewScreen((TitleScreen) (Object) this)
                    );
                }
        );

        addRenderableWidget(ctnhChangelogButton);
    }

    @Override
    public void tick() {
        super.tick();

        // 修改：只有启用了版本检查才更新状态
        if (Config.isEnableVersionCheck() && VersionCheckService.isCheckDone()) {
            ctnhHasUpdate = VersionCheckService.hasUpdate();
        } else {
            ctnhHasUpdate = false;
        }

        if (ctnhHasUpdate) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - ctnhLastBlinkTime > BLINK_INTERVAL) {
                ctnhLastBlinkTime = currentTime;
                ctnhBlinkState = !ctnhBlinkState;
            }
        } else {
            ctnhBlinkState = false;
        }
    }

    @Unique
    private class ChangelogButton extends Button {
        public ChangelogButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            if (ctnhHasUpdate && ctnhBlinkState) {
                int x = this.getX();
                int y = this.getY();
                int width = this.getWidth();
                int height = this.getHeight();

                int borderWidth = 1;

                graphics.fill(x, y, x + width, y + borderWidth, BORDER_COLOR);
                graphics.fill(x, y + height - borderWidth, x + width, y + height, BORDER_COLOR);
                graphics.fill(x, y, x + borderWidth, y + height, BORDER_COLOR);
                graphics.fill(x + width - borderWidth, y, x + width, y + height, BORDER_COLOR);
            }
        }
    }
}