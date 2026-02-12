package io.github.cpearl0.ctnhchangelog.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

public class ChangelogList extends ObjectSelectionList<ChangelogList.Entry> {

    public ChangelogList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
        super(minecraft, width, height, y0, y1, itemHeight);
        
        for (ChangelogEntry entry : ChangelogEntry.getAllEntries()) {
            this.addEntry(new Entry(entry));
        }
    }
    
    @Override
    public int getRowWidth() {
        return this.width - 40;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final ChangelogEntry changelogEntry;
        
        public Entry(ChangelogEntry entry) {
            this.changelogEntry = entry;
        }
        
        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            Font font = ChangelogList.this.minecraft.font;
            
            if (hovering) {
                graphics.fill(left - 2, top - 2, left + width + 2, top + height + 2, 0x80000000);
            }
            
            int borderColor = changelogEntry.getColor();
            graphics.fill(left, top, left + 4, top + height, borderColor | 0xFF000000);
            
            int textLeft = left + 12;
            int line1Y = top + 4;
            int line2Y = top + 18;
            int line3Y = top + 32;
            
            // Version and type
            String typeIcon = getTypeIcon(changelogEntry.getType());
            graphics.drawString(font, typeIcon + " " + changelogEntry.getVersion(), textLeft, line1Y, changelogEntry.getColor() | 0xFF000000);
            
            // Type tag
            String typeTag = getTypeTag(changelogEntry.getType());
            if (typeTag != null) {
                int versionWidth = font.width(typeIcon + " " + changelogEntry.getVersion());
                int tagX = textLeft + versionWidth + 6;
                int tagWidth = font.width(typeTag) + 6;
                int tagHeight = 10;
                int tagBgColor = changelogEntry.getType().equals("major") ? 0xFF55FF55 : 
                                 changelogEntry.getType().equals("minor") ? 0xFF5555FF : 0xFFFFFF55;
                graphics.fill(tagX, line1Y - 1, tagX + tagWidth, line1Y + tagHeight, tagBgColor);
                graphics.drawString(font, typeTag, tagX + 3, line1Y, 0xFFFFFFFF);
            }
            
            // Date
            if (!changelogEntry.getDate().isEmpty()) {
                String dateText = "日期: " + changelogEntry.getDate();
                int dateWidth = font.width(dateText);
                graphics.drawString(font, dateText, left + width - dateWidth - 10, line1Y, 0xFFAAAAAA);
            }
            
            // Title
            if (!changelogEntry.getTitle().isEmpty()) {
                graphics.drawString(font, changelogEntry.getTitle(), textLeft, line2Y, 0xFFDDDDDD);
            }
            
            // Changes preview (first line)
            if (!changelogEntry.getChanges().isEmpty()) {
                String preview = "• " + changelogEntry.getChanges().get(0);
                if (preview.length() > 60) {
                    preview = preview.substring(0, 57) + "...";
                }
                graphics.drawString(font, preview, textLeft, line3Y, 0xFFAAAAAA);
                
                if (changelogEntry.getChanges().size() > 1) {
                    String moreText = "还有 " + (changelogEntry.getChanges().size() - 1) + " 项更改...";
                    graphics.drawString(font, moreText, textLeft + 250, line3Y, 0xFF888888);
                }
            }
        }
        
        @Override
        public Component getNarration() {
            return Component.literal(changelogEntry.getVersion() + " - " + changelogEntry.getTitle());
        }
        
        public ChangelogEntry getEntry() {
            return changelogEntry;
        }
        
        private String getTypeIcon(String type) {
            return switch (type) {
                case "major" -> "★";
                case "minor" -> "●";
                case "patch" -> "○";
                case "fix" -> "◆";
                default -> "•";
            };
        }
        
        private String getTypeTag(String type) {
            return switch (type) {
                case "major" -> "重大更新";
                case "minor" -> "功能更新";
                case "patch" -> "修复";
                case "fix" -> "热修复";
                default -> null;
            };
        }
    }
}