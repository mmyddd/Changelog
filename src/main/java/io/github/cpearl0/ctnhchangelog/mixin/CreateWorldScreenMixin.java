package io.github.cpearl0.ctnhchangelog.mixin;

import io.github.cpearl0.ctnhchangelog.Config;
import io.github.cpearl0.ctnhchangelog.client.ChangelogList;
import io.github.cpearl0.ctnhchangelog.client.ChangelogTab;
import io.github.cpearl0.ctnhchangelog.mixin.accessor.TabNavigationBarAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    @Final
    private TabManager tabManager;

    @Unique
    private static final Component VIEW_CHANGELOG_TEXT = Component.translatable("ctnhchangelog.button.view_changelog");

    @Unique
    private Component ctnhchangelog$originalCreateButtonText = null;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private Button ctnhchangelog$findCreateButton() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button) {
                String msg = button.getMessage().getString();
                if (msg.contains("创建") || msg.contains("Create") || msg.equals(VIEW_CHANGELOG_TEXT.getString())) {
                    return button;
                }
            }
        }
        return null;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;
        
        if (ChangelogTab.shouldOpenChangelogTab) {
            ChangelogTab.shouldOpenChangelogTab = false;

            for (Tab tab : this.ctnhchangelog$getAllTabs()) {
                if (tab instanceof ChangelogTab) {
                    this.tabManager.setCurrentTab(tab, true);
                    break;
                }
            }
        }
    }

    @Unique
    private Iterable<Tab> ctnhchangelog$getAllTabs() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof TabNavigationBar navBar) {
                return ((TabNavigationBarAccessor) navBar).getTabs();
            }
        }
        return Collections.emptyList();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;
        
        Tab currentTab = this.tabManager.getCurrentTab();
        Button createButton = this.ctnhchangelog$findCreateButton();
        
        if (createButton != null) {
            if (currentTab instanceof ChangelogTab) {
                if (this.ctnhchangelog$originalCreateButtonText == null) {
                    this.ctnhchangelog$originalCreateButtonText = createButton.getMessage();
                }

                if (!createButton.getMessage().equals(VIEW_CHANGELOG_TEXT)) {
                    createButton.setMessage(VIEW_CHANGELOG_TEXT);
                }
            } else if (this.ctnhchangelog$originalCreateButtonText != null && createButton.getMessage().equals(VIEW_CHANGELOG_TEXT)) {
                createButton.setMessage(this.ctnhchangelog$originalCreateButtonText);
            }
        }

        if (currentTab instanceof ChangelogTab changelogTab) {
            changelogTab.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!Config.isChangelogTabEnabled()) return super.mouseScrolled(mouseX, mouseY, delta);

        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab instanceof ChangelogTab changelogTab) {
            ChangelogList list = changelogTab.getChangelogList();
            if (list != null && list.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Config.isChangelogTabEnabled()) return super.mouseClicked(mouseX, mouseY, button);
        
        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab instanceof ChangelogTab changelogTab) {
            ChangelogList list = changelogTab.getChangelogList();
            if (list != null && list.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!Config.isChangelogTabEnabled()) return super.mouseReleased(mouseX, mouseY, button);
        
        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab instanceof ChangelogTab changelogTab) {
            ChangelogList list = changelogTab.getChangelogList();
            if (list != null && list.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!Config.isChangelogTabEnabled()) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        
        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab instanceof ChangelogTab changelogTab) {
            ChangelogList list = changelogTab.getChangelogList();
            if (list != null && list.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}