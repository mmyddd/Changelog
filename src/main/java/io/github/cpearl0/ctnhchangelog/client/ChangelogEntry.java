package io.github.cpearl0.ctnhchangelog.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cpearl0.ctnhchangelog.CTNHChangelog;
import io.github.cpearl0.ctnhchangelog.Config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChangelogEntry {
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    
    private final String version;
    private final String date;
    private final String title;
    private final List<String> changes;
    private final String type;
    private final int color;
    
    private static List<ChangelogEntry> ALL_ENTRIES = new ArrayList<>();
    private static boolean isLoaded = false;
    
    public ChangelogEntry(String version, String date, String title, List<String> changes, String type, int color) {
        this.version = version;
        this.date = date;
        this.title = title;
        this.changes = changes;
        this.type = type;
        this.color = color;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getTitle() {
        return title;
    }
    
    public List<String> getChanges() {
        return changes;
    }
    
    public String getType() {
        return type;
    }
    
    public int getColor() {
        return color;
    }
    
    public static List<ChangelogEntry> getAllEntries() {
        return ALL_ENTRIES;
    }

    public static boolean isLoaded() {
        return isLoaded;
    }

    public static void loadAsync() {
        CompletableFuture.runAsync(() -> {
            String remoteUrl = Config.getChangelogUrl();
            if (remoteUrl != null && !remoteUrl.isEmpty()) {
                if (loadFromRemote(remoteUrl)) {
                    CTNHChangelog.LOGGER.info("Loaded {} changelog entries from remote", ALL_ENTRIES.size());
                    isLoaded = true;
                    return;
                }
                CTNHChangelog.LOGGER.warn("Failed to load from remote, falling back to local resources");
            }
            loadFromResources();
            isLoaded = true;
        });
    }
    
    private static boolean loadFromRemote(String urlStr) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "CTNH-Changelog/1.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                CTNHChangelog.LOGGER.warn("Remote JSON returned status code: {}", responseCode);
                return false;
            }
            
            try (InputStream is = connection.getInputStream()) {
                return loadFromStream(is);
            }
        } catch (Exception e) {
            CTNHChangelog.LOGGER.warn("Failed to load changelog from remote: {}", e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    public static void loadFromResources() {
        try {
            InputStream is = ChangelogEntry.class.getResourceAsStream("/changelog.json");
            if (is != null) {
                if (loadFromStream(is)) {
                    CTNHChangelog.LOGGER.info("Loaded {} changelog entries from resources", ALL_ENTRIES.size());
                } else {
                    loadDefaultEntries();
                }
            } else {
                CTNHChangelog.LOGGER.info("Could not find changelog.json in resources, using defaults");
                loadDefaultEntries();
            }
        } catch (Exception e) {
            CTNHChangelog.LOGGER.error("Failed to load changelog from resources", e);
            loadDefaultEntries();
        }
    }
    
    private static boolean loadFromStream(InputStream is) {
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray entriesArray = root.getAsJsonArray("entries");
            List<ChangelogEntry> entries = new ArrayList<>();
            
            for (JsonElement element : entriesArray) {
                JsonObject obj = element.getAsJsonObject();
                String version = obj.get("version").getAsString();
                String date = obj.has("date") ? obj.get("date").getAsString() : "";
                String title = obj.has("title") ? obj.get("title").getAsString() : "";
                
                List<String> changes = new ArrayList<>();
                if (obj.has("changes")) {
                    JsonArray changesArray = obj.getAsJsonArray("changes");
                    for (JsonElement change : changesArray) {
                        changes.add(change.getAsString());
                    }
                }
                
                String type = obj.has("type") ? obj.get("type").getAsString() : "update";
                String colorStr = obj.has("color") ? obj.get("color").getAsString() : "#FFFFFF";
                int color = parseColor(colorStr);
                
                entries.add(new ChangelogEntry(version, date, title, changes, type, color));
            }
            
            ALL_ENTRIES = entries;
            return true;
        } catch (Exception e) {
            CTNHChangelog.LOGGER.error("Failed to parse changelog JSON", e);
            return false;
        }
    }
    
    private static int parseColor(String colorStr) {
        try {
            if (colorStr.startsWith("0x") || colorStr.startsWith("0X")) {
                return (int) Long.parseLong(colorStr.substring(2), 16);
            } else if (colorStr.startsWith("#")) {
                return (int) Long.parseLong("FF" + colorStr.substring(1), 16);
            } else {
                return Integer.parseInt(colorStr);
            }
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }
    
    private static void loadDefaultEntries() {
        ALL_ENTRIES = new ArrayList<>();
        
        List<String> changes1 = new ArrayList<>();
        changes1.add("新增更新日志查看功能");
        changes1.add("优化界面显示效果");
        changes1.add("修复已知问题");
        ALL_ENTRIES.add(new ChangelogEntry("1.0.0", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), 
                "首次发布", changes1, "major", 0x55FF55));
        
        CTNHChangelog.LOGGER.info("Loaded default changelog entries");
    }
}