package com.mmyddd.mcmod.changelog;

import com.mmyddd.mcmod.changelog.client.ChangelogEntry;
import com.mmyddd.mcmod.changelog.client.VersionCheckService;
import lombok.Getter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = CTNHChangelog.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> CHANGELOG_URL;
    private static final ForgeConfigSpec.ConfigValue<String> MODPACK_VERSION;
    private static final ForgeConfigSpec.BooleanValue ENABLE_CHANGELOG_TAB;
    private static final ForgeConfigSpec.BooleanValue ENABLE_VERSION_CHECK;

    static final ForgeConfigSpec SPEC;

    @Getter
    private static String changelogUrl = "";
    @Getter
    private static String modpackVersion = "";
    private static boolean enableChangelogTab = true;
    @Getter
    private static boolean enableVersionCheck = true; // 默认启用

    static {
        CHANGELOG_URL = BUILDER
                .comment("更新日志JSON文件的远程URL", "例如: http://example.com/changelog.json")
                .define("changelogUrl", "");

        MODPACK_VERSION = BUILDER
                .comment("当前整合包版本号", "用于与更新日志最新版本对比", "例如: 1.0.0")
                .define("ModpackVersion", "1.0.0");

        ENABLE_CHANGELOG_TAB = BUILDER
                .comment("是否在创建世界界面显示更新日志标签页")
                .define("enableChangelogTab", true);

        ENABLE_VERSION_CHECK = BUILDER
                .comment("是否启用版本更新检查", "如果禁用，将不会对比ModpackVersion和远程最新版本", "也不会显示更新提示")
                .define("enableVersionCheck", true);

        SPEC = BUILDER.build();
    }

    public static boolean isChangelogTabEnabled() {
        return enableChangelogTab;
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            changelogUrl = CHANGELOG_URL.get();
            modpackVersion = MODPACK_VERSION.get();
            enableChangelogTab = ENABLE_CHANGELOG_TAB.get();
            enableVersionCheck = ENABLE_VERSION_CHECK.get();

            CTNHChangelog.LOGGER.info("Config loaded - changelogUrl: {}, modpackVersion: {}, enableChangelogTab: {}, enableVersionCheck: {}",
                    changelogUrl, modpackVersion, enableChangelogTab, enableVersionCheck);

            ChangelogEntry.loadAfterConfig();
        }
    }
}