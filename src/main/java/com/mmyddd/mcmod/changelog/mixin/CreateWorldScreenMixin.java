// CreateWorldScreenMixin.java
package io.github.cpearl0.ctnhchangelog.mixin;

import io.github.cpearl0.ctnhchangelog.CTNHChangelog;
import io.github.cpearl0.ctnhchangelog.Config;
import io.github.cpearl0.ctnhchangelog.client.ChangelogList;
import io.github.cpearl0.ctnhchangelog.client.ChangelogScreen;
import io.github.cpearl0.ctnhchangelog.client.ChangelogTab;
import io.github.cpearl0.ctnhchangelog.mixin.accessor.TabNavigationBarAccessor;
import net.minecraft.client.Minecraft;
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

import java.lang.reflect.Field;
import java.util.Collections;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    @Final
    private TabManager tabManager;

    @Unique
    private static final Component VIEW_CHANGELOG_TEXT = Component.translatable("ctnhchangelog.button.view_changelog");

    @Unique
    private static final Component CREATE_WORLD_TEXT = Component.translatable("selectWorld.create");

    @Unique
    private Component ctnhchangelog$originalCreateButtonText = null;

    @Unique
    private Button.OnPress ctnhchangelog$originalPressCommand = null;

    @Unique
    private Button ctnhchangelog$currentButton = null;

    @Unique
    private boolean ctnhchangelog$isUpdatingButton = false;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private Button ctnhchangelog$findCreateButton() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button) {
                Component msg = button.getMessage();
                String msgStr = msg.getString();
                if (msgStr.contains("创建") ||
                        msgStr.contains("Create") ||
                        msg.equals(CREATE_WORLD_TEXT) ||
                        msgStr.contains("查看详情") ||
                        msgStr.contains("View Details") ||
                        msg.equals(VIEW_CHANGELOG_TEXT)) {
                    return button;
                }
            }
        }
        return null;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;

        // 重置按钮状态
        this.ctnhchangelog$resetButtonState();

        // 主动触发一次按钮更新
        this.ctnhchangelog$updateButtonForCurrentTab();

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

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void onRepositionElements(CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;

        // 窗口大小变化时重新定位按钮
        this.ctnhchangelog$updateButtonPosition();
    }

    @Unique
    private void ctnhchangelog$resetButtonState() {
        this.ctnhchangelog$originalCreateButtonText = null;
        this.ctnhchangelog$originalPressCommand = null;
        this.ctnhchangelog$currentButton = null;
    }

    @Unique
    private void ctnhchangelog$updateButtonPosition() {
        if (this.ctnhchangelog$isUpdatingButton) return;

        Button currentButton = this.ctnhchangelog$findCreateButton();
        if (currentButton != null && this.ctnhchangelog$currentButton != null) {
            // 确保当前按钮位置正确
            Tab currentTab = this.tabManager.getCurrentTab();
            if (currentTab instanceof ChangelogTab) {
                // 如果当前是查看详情按钮，更新其位置
                if (this.ctnhchangelog$currentButton.getMessage().getString().equals(VIEW_CHANGELOG_TEXT.getString())) {
                    this.ctnhchangelog$currentButton.setX(currentButton.getX());
                    this.ctnhchangelog$currentButton.setY(currentButton.getY());
                }
            }
        }
    }

    @Unique
    private void ctnhchangelog$updateButtonForCurrentTab() {
        if (this.ctnhchangelog$isUpdatingButton) return;

        try {
            this.ctnhchangelog$isUpdatingButton = true;

            Tab currentTab = this.tabManager.getCurrentTab();
            Button currentCreateButton = this.ctnhchangelog$findCreateButton();

            if (currentCreateButton != null) {
                if (currentTab instanceof ChangelogTab) {
                    this.ctnhchangelog$switchToViewButton(currentCreateButton);
                } else {
                    this.ctnhchangelog$switchToCreateButton(currentCreateButton);
                }
            }
        } finally {
            this.ctnhchangelog$isUpdatingButton = false;
        }
    }

    @Unique
    private void ctnhchangelog$switchToViewButton(Button currentCreateButton) {
        // 保存原始按钮信息
        if (this.ctnhchangelog$originalCreateButtonText == null) {
            this.ctnhchangelog$originalCreateButtonText = currentCreateButton.getMessage();
        }
        if (this.ctnhchangelog$originalPressCommand == null) {
            try {
                Field pressField = Button.class.getDeclaredField("onPress");
                pressField.setAccessible(true);
                this.ctnhchangelog$originalPressCommand = (Button.OnPress) pressField.get(currentCreateButton);
            } catch (Exception e) {
                CTNHChangelog.LOGGER.error("Failed to access button onPress field", e);
            }
        }

        // 如果当前按钮不是查看详情按钮，才进行替换
        if (!currentCreateButton.getMessage().getString().equals(VIEW_CHANGELOG_TEXT.getString())) {
            this.removeWidget(currentCreateButton);

            Button viewButton = Button.builder(VIEW_CHANGELOG_TEXT, button -> {
                Tab currentTab = this.tabManager.getCurrentTab();
                if (currentTab instanceof ChangelogTab changelogTab) {
                    ChangelogList list = changelogTab.getChangelogList();
                    ChangelogList.Entry selected = list != null ? list.getSelected() : null;
                    if (selected != null) {
                        Minecraft.getInstance().setScreen(
                                new ChangelogScreen(
                                        selected.getEntry(),
                                        (CreateWorldScreen) (Object) CreateWorldScreenMixin.this
                                )
                        );
                    }
                }
            }).bounds(currentCreateButton.getX(), currentCreateButton.getY(),
                    currentCreateButton.getWidth(), currentCreateButton.getHeight()).build();

            this.addRenderableWidget(viewButton);
            this.ctnhchangelog$currentButton = viewButton;
        }
    }

    @Unique
    private void ctnhchangelog$switchToCreateButton(Button currentCreateButton) {
        // 只有当当前按钮是我们创建的查看详情按钮时，才恢复
        if (this.ctnhchangelog$originalCreateButtonText != null &&
                this.ctnhchangelog$originalPressCommand != null &&
                this.ctnhchangelog$currentButton == currentCreateButton &&
                currentCreateButton.getMessage().getString().equals(VIEW_CHANGELOG_TEXT.getString())) {

            this.removeWidget(currentCreateButton);

            Button originalButton = Button.builder(
                            this.ctnhchangelog$originalCreateButtonText,
                            this.ctnhchangelog$originalPressCommand)
                    .bounds(currentCreateButton.getX(), currentCreateButton.getY(),
                            currentCreateButton.getWidth(), currentCreateButton.getHeight())
                    .build();

            this.addRenderableWidget(originalButton);
            this.ctnhchangelog$currentButton = originalButton;
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

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;

        // 每次渲染前检查并更新按钮状态
        this.ctnhchangelog$updateButtonForCurrentTab();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!Config.isChangelogTabEnabled()) return;

        Tab currentTab = this.tabManager.getCurrentTab();
        if (currentTab instanceof ChangelogTab changelogTab) {
            changelogTab.render(graphics, mouseX, mouseY, partialTick);
        }
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
}