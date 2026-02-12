package io.github.cpearl0.ctnhchangelog.mixin;

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

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private Button ctnhchangelog$findCreateButton() {
        for (GuiEventListener child : this.children()) {
            if (child instanceof Button button) {
                Component msg = button.getMessage();
                // 匹配创建世界按钮
                if (msg.getString().contains("创建") ||
                        msg.getString().contains("Create") ||
                        msg.equals(CREATE_WORLD_TEXT)) {
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
                // 切换到更新日志标签时，修改按钮文本和功能
                if (this.ctnhchangelog$originalCreateButtonText == null) {
                    this.ctnhchangelog$originalCreateButtonText = createButton.getMessage();
                }
                if (this.ctnhchangelog$originalPressCommand == null) {
                    try {
                        Field pressField = Button.class.getDeclaredField("onPress");
                        pressField.setAccessible(true);
                        this.ctnhchangelog$originalPressCommand = (Button.OnPress) pressField.get(createButton);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // 如果当前按钮不是我们创建的新按钮，或者文本不是查看详情，才进行替换
                if (this.ctnhchangelog$currentButton != createButton ||
                        !createButton.getMessage().getString().equals(VIEW_CHANGELOG_TEXT.getString())) {

                    Button newButton = Button.builder(VIEW_CHANGELOG_TEXT, button -> {
                        ChangelogTab changelogTab = (ChangelogTab) currentTab;
                        ChangelogList list = changelogTab.getChangelogList();
                        ChangelogList.Entry selected = list.getSelected();
                        if (selected != null) {
                            Minecraft.getInstance().setScreen(
                                    new ChangelogScreen(
                                            selected.getEntry(),
                                            (CreateWorldScreen) (Object) CreateWorldScreenMixin.this
                                    )
                            );
                        }
                    }).bounds(createButton.getX(), createButton.getY(), createButton.getWidth(), createButton.getHeight()).build();

                    this.removeWidget(createButton);
                    this.addRenderableWidget(newButton);
                    this.ctnhchangelog$currentButton = newButton;
                }
            } else {
                // 切出时恢复原样
                if (this.ctnhchangelog$originalCreateButtonText != null &&
                        this.ctnhchangelog$originalPressCommand != null &&
                        this.ctnhchangelog$currentButton != null) {

                    Button originalButton = Button.builder(this.ctnhchangelog$originalCreateButtonText,
                                    this.ctnhchangelog$originalPressCommand)
                            .bounds(createButton.getX(), createButton.getY(), createButton.getWidth(), createButton.getHeight())
                            .build();

                    this.removeWidget(createButton);
                    this.addRenderableWidget(originalButton);
                    this.ctnhchangelog$currentButton = originalButton;
                }
            }
        }

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
            if (list != null) {
                if (list.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
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