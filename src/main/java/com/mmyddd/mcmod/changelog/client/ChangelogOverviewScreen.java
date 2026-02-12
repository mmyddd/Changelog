package com.mmyddd.mcmod.changelog.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import java.util.List;

public class ChangelogOverviewScreen extends Screen {
    private final Screen parentScreen;
    private ChangelogList changelogList;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int listRight;

    public ChangelogOverviewScreen(Screen parent) {
        super(Component.translatable("ctnhchangelog.tab.title"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();

        // 计算列表区域
        this.listLeft = 20;
        this.listRight = this.width - 20;
        this.listTop = 40;
        this.listBottom = this.height - 60;

        // 创建更新日志列表
        this.changelogList = new ChangelogList(
                this.minecraft,
                this.width - 40,
                this.height - 100,
                this.listTop,
                this.listBottom,
                52
        );
        this.addRenderableWidget(changelogList);

        // 添加返回按钮
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.back"),
                                button -> this.minecraft.setScreen(parentScreen)
                        )
                        .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                        .build()
        );

        // 添加刷新按钮（可选）
        this.addRenderableWidget(
                Button.builder(
                                Component.literal("↻"),
                                button -> {
                                    ChangelogEntry.loadAsync();
                                    this.minecraft.setScreen(new ChangelogOverviewScreen(parentScreen));
                                }
                        )
                        .bounds(this.width - 30, 10, 20, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        // 渲染标题
        String title = this.title.getString();
        graphics.drawString(
                this.font,
                title,
                this.width / 2 - this.font.width(title) / 2,
                20,
                0xFFFFFF
        );

        // 渲染统计信息
        List<ChangelogEntry> entries = ChangelogEntry.getAllEntries();
        String stats = Component.translatable(
                "ctnhchangelog.stats",
                entries.size()
        ).getString();
        graphics.drawString(
                this.font,
                stats,
                20,
                25,
                0xAAAAAA
        );

        // 渲染页脚（渐变文本）
        String footer = ChangelogEntry.getFooterText();
        if (footer != null && !footer.isEmpty()) {
            int footerY = this.height - 50;
            int textWidth = this.font.width(footer);
            int startX = this.width / 2 - textWidth / 2;

            // 渐变渲染（如果需要）
            graphics.drawString(
                    this.font,
                    footer,
                    startX,
                    footerY,
                    0x55FF55
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (changelogList != null && changelogList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (changelogList != null && changelogList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}