package net.citizensnpcs;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.util.Util;

public class Settings {
    private final Storage config;
    private final DataKey root;

    public Settings(File folder) {
        config = new YamlStorage(new File(folder, "config.yml"), "Citizens Configuration");
        root = config.getKey("");

        config.load();
        for (Setting setting : Setting.values()) {
            if (!root.keyExists(setting.path)) {
                setting.setAtKey(root);
            } else {
                setting.loadFromKey(root);
            }
        }
        updateMessagingSettings();
        save();
    }

    public void reload() {
        config.load();
        for (Setting setting : Setting.values()) {
            if (root.keyExists(setting.path)) {
                setting.loadFromKey(root);
            }
        }
        updateMessagingSettings();
        save();
    }

    public void save() {
        config.save();
    }

    private void updateMessagingSettings() {
        File file = null;
        if (!Setting.DEBUG_FILE.asString().isEmpty()) {
            file = new File(CitizensAPI.getPlugin().getDataFolder(), Setting.DEBUG_FILE.asString());
        }
        Messaging.configure(file, Setting.DEBUG_MODE.asBoolean(), Setting.MESSAGE_COLOUR.asString(),
                Setting.HIGHLIGHT_COLOUR.asString(), Setting.ERROR_COLOUR.asString());
    }

    public enum Setting {
        ALWAYS_USE_NAME_HOLOGRAM("始终使用全息图显示名称，而不是仅用于十六进制颜色/占位符",
                                         "npc.always-use-name-holograms", false),
        ASTAR_ITERATIONS_PER_TICK("每个刻刷新搜索的方块数量（Citizens 寻路器）",
                                          "npc.pathfinding.new-finder.iterations-per-tick", "npc.pathfinding.new-finder.iterations-per-tick",
                                          250),
        AUTH_SERVER_URL("使用此URL搜索游戏配置文件", "general.authlib.profile-url",
                                "https://sessionserver.mojang.com/session/minecraft/profile/"),
        BOSSBAR_RANGE("默认的bossbar范围，以方块为单位", "npc.default.bossbar-view-range", 64),
        CHAT_BYSTANDERS_HEAR_TARGETED_CHAT(
                "是否让附近的玩家也能听到聊天内容，即使是针对特定玩家的",
                        "npc.chat.options.bystanders-hear-targeted-chat", false),
        CHAT_FORMAT("默认的文本格式（启用占位符）", "npc.chat.format.no-targets", "[<npc>]: <text>"),
        CHAT_FORMAT_TO_BYSTANDERS("附近玩家的默认文本格式（启用占位符）",
                                          "npc.chat.format.with-target-to-bystanders", "[<npc>] -> [<target>]: <text>"),
        CHAT_FORMAT_TO_TARGET("目标文本的默认格式（启用占位符）",
                                      "npc.chat.format.to-target", "<npc>: <text>"),
        CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS("附近玩家的默认文本格式（启用占位符）",
                                                       "npc.chat.format.with-targets-to-bystanders", "[<npc>] -> [<targets>]: <text>"),
        CHAT_MAX_NUMBER_OF_TARGETS("最多向旁观者显示的目标名称数量",
                                           "npc.chat.options.max-number-of-targets-to-show", 2),
        CHAT_MULTIPLE_TARGETS_FORMAT("npc.chat.options.multiple-targets-format",
                                             "<target>|, <target>| & <target>| & others"),
        CHAT_RANGE("附近玩家的范围，以方块为单位", "npc.chat.options.range", 5),
        CHECK_MINECRAFT_VERSION("是否检查Minecraft版本以确保兼容性（请勿更改）",
                                        "advanced.check-minecraft-version", true),
        CONTROLLABLE_GROUND_DIRECTION_MODIFIER("控制NPC时地面速度的百分比增益",
                                                       "npc.controllable.ground-direction-modifier", 1.0D),
        DEBUG_CHUNK_LOADS("调试区块加载堆栈跟踪，在最近的Minecraft版本中不太有用",
                                  "general.debug-chunk-loads", false),
        DEBUG_FILE("将Citizens的调试输出发送到特定文件", "general.debug-file", ""),
        DEBUG_MODE("启用Citizens调试", "general.debug-mode", false),
        DEBUG_PATHFINDING("通过显示虚假目标方块调试寻路", "npc.pathfinding.debug",
                                  "npc.pathfinding.debug-paths", false),
        DEFAULT_BLOCK_BREAKER_RADIUS(
                "默认的破坏方块半径，以方块为单位<br>如果大于0，NPC将寻路到离目标方块这个距离",
                        "npc.defaults.block-breaker-radius", "npc.default.block-breaker-radius", -1),
        DEFAULT_CACHE_WAYPOINT_PATHS(
                "是否默认缓存/npc路径<br>可以消除重复的静态路径寻路",
                        "npc.default.waypoints.cache-paths", false),
        DEFAULT_DESTINATION_TELEPORT_MARGIN(
                "NPC到达目的地时默认的距离，以方块为单位<br>用于确保精确到达目的地",
                        "npc.pathfinding.defaults.destination-teleport-margin",
                        "npc.pathfinding.default-destination-teleport-margin", -1),
        DEFAULT_DISTANCE_MARGIN(
                "默认的移动距离，以方块为单位，NPC在认为路径完成之前会先移动此距离<br>注意：这与寻路距离不同，寻路距离由path-distance-margin指定",
                        "npc.pathfinding.default-distance-margin", 1),
        DEFAULT_HOLOGRAM_BACKGROUND_COLOR(
                "默认的全息图背景颜色，指定为RGB或RGBA值<br>例如 0,255,123 是绿色，255,255,255,255 是透明",
                        "npc.hologram.default-background-color", ""),
        DEFAULT_HOLOGRAM_RENDERER(
                "默认的全息图渲染器，必须是以下之一：<br>interaction - 需要1.19+，比display更接近名牌效果<br>display - 允许不同的背景颜色<br>display_vehicle - 将显示附加到NPC上<br>areaeffectcloud - 最安全的选项<br>armorstand - 第二安全的选项，客户端有碰撞盒<br>armorstand_vehicle - 将盔甲架附加到NPC上，仅用于名字板",
                        "npc.hologram.default-renderer", "display"),
        DEFAULT_LOOK_CLOSE("默认启用近距离注视", "npc.default.look-close.enabled", false),
        DEFAULT_LOOK_CLOSE_RANGE("默认的近距离注视范围，以方块为单位", "npc.default.look-close.range", 10),
        DEFAULT_NPC_HOLOGRAM_LINE_HEIGHT("默认的全息图行间距", "npc.hologram.default-line-height",
                                                 0.4D),
        DEFAULT_NPC_LIMIT(
                "单个玩家拥有的NPC最大数量（给与citizens ignore-limits权限以跳过此检查）",
                        "npc.limits.default-limit", 10),
        DEFAULT_PATH_DISTANCE_MARGIN(
                "默认的寻路完成距离，以方块为单位<br>与移动距离不同，移动距离由distance-margin指定<br>如果要精确到达目标位置，请设置为0",
                        "npc.pathfinding.default-path-distance-margin", 1),
        DEFAULT_PATHFINDER_UPDATE_PATH_RATE("目标为动态目标（如实体）时重新计算路径的频率",
                                                    "npc.pathfinding.update-path-rate", "1s"),
        DEFAULT_PATHFINDING_RANGE(
                "默认的寻路范围，以方块为单位<br>不要设置得太高以避免卡顿，尝试用较短的路径段进行寻路",
                        "npc.default.pathfinding.range", "npc.pathfinding.default-range-blocks", 75F),
        DEFAULT_RANDOM_LOOK_CLOSE("默认启用随机近距离注视", "npc.default.look-close.random-look-enabled",
                                          false),
        DEFAULT_RANDOM_LOOK_DELAY("默认的随机注视延迟", "npc.default.look-close.random-look-delay", "3s"),
        DEFAULT_RANDOM_TALKER("默认与附近玩家交谈", "npc.default.random-talker",
                                      "npc.default.talk-close.random-talker", false),
        DEFAULT_REALISTIC_LOOKING("默认检查视线是否可见玩家", "npc.default.realistic-looking", "npc.default.look-close.realistic-looking", false),
        DEFAULT_SPAWN_NODAMAGE_DURATION(
                "实体出生时的默认无敌持续时间，Minecraft默认是20刻",
                        "npc.default.spawn-nodamage-duration", "npc.default.spawn-invincibility-duration", "1s"),
        DEFAULT_STATIONARY_DURATION(
                "NPC在同一位置停留的默认时间，超过此时间视为卡住，寻路失败",
                        "npc.default.stationary-duration", "npc.pathfinding.default-stationary-duration", -1),
        DEFAULT_STRAIGHT_LINE_TARGETING_DISTANCE(
                "当NPC不再使用寻路，而是直接朝目标走时的距离，以方块为单位<br>目前仅适用于动态目标，如实体",
                        "npc.pathfinding.straight-line-targeting-distance", 5),
        DEFAULT_STUCK_ACTION(
                "当NPC无法找到路径或长时间卡住在同一方块时执行的默认动作<br>支持的选项是：'传送到目标' 或 '无'",
                        "npc.pathfinding.default-stuck-action", "none"),
        DEFAULT_TALK_CLOSE("npc.default.talk-close.enabled", false),
        DEFAULT_TALK_CLOSE_RANGE("默认与玩家交谈的范围，以方块为单位", "npc.default.talk-close.range", 5),
        DEFAULT_TEXT("npc.default.talk-close.text", "嗨，我是<npc>！") {
            @SuppressWarnings("unchecked")
            @Override
            public void loadFromKey(DataKey root) {
                List<String> list = new ArrayList<>();
                Object raw = root.getRaw(path);
                if (raw instanceof ConfigurationSection || raw instanceof Map) {
                    for (DataKey key : root.getRelative(path).getSubKeys()) {
                        list.add(key.getString(""));
                    }
                } else if (raw instanceof Collection) {
                    list.addAll((Collection<? extends String>) raw);
                }
                value = list;
            }

            @Override
            protected void setAtKey(DataKey root) {
                root.setRaw(path, Lists.newArrayList(value));
                setComments(root);
            }
        },
        DEFAULT_TEXT_DELAY_MAX("与玩家交谈时的默认最大延迟",
                                       "npc.text.default-random-text-delay-max", "10s"),
        DEFAULT_TEXT_DELAY_MIN("与玩家交谈时的默认最小延迟",
                                       "npc.text.default-random-text-delay-min", "5s"),
        DEFAULT_TEXT_SPEECH_BUBBLE_DURATION("默认的语音气泡显示时间",
                                                    "npc.text.speech-bubble-ticks", "npc.text.speech-bubble-duration", "50"),
        USE_SCOREBOARD_TEAMS("npc.scoreboard-teams.enable", "npc.defaults.enable-scoreboard-teams", true),
        WARN_ON_RELOAD("general.reload-warning-enabled", true),;

        private String comments;
        private Duration duration;
        private String migrate;
        protected final String path;
        protected Object value;

        Setting(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        Setting(String migrateOrComments, String path, Object value) {
            if (migrateOrComments.contains(".") && !migrateOrComments.contains(" ")) {
                migrate = migrateOrComments;
            } else {
                comments = migrateOrComments;
            }
            this.path = path;
            this.value = value;
        }

        Setting(String comments, String migrate, String path, Object value) {
            this.migrate = migrate;
            this.comments = comments;
            this.path = path;
            this.value = value;
        }

        public boolean asBoolean() {
            return (Boolean) value;
        }

        public double asDouble() {
            return ((Number) value).doubleValue();
        }

        public float asFloat() {
            return ((Number) value).floatValue();
        }

        public int asInt() {
            if (value instanceof String)
                return Integer.parseInt(value.toString());
            return ((Number) value).intValue();
        }

        @SuppressWarnings("unchecked")
        public List<String> asList() {
            if (!(value instanceof List)) {
                value = Lists.newArrayList(value);
            }
            return (List<String>) value;
        }

        public int asSeconds() {
            if (duration == null) {
                duration = SpigotUtil.parseDuration(asString(), null);
            }
            return Util.convert(TimeUnit.SECONDS, duration);
        }

        public String asString() {
            return value.toString();
        }

        public int asTicks() {
            if (duration == null) {
                duration = SpigotUtil.parseDuration(asString(), null);
            }
            return Util.toTicks(duration);
        }

        protected void loadFromKey(DataKey root) {
            setComments(root);
            if (migrate != null && root.keyExists(migrate) && !root.keyExists(path)) {
                value = root.getRaw(migrate);
                root.removeKey(migrate);
            } else {
                value = root.getRaw(path);
            }
        }

        protected void setAtKey(DataKey root) {
            root.setRaw(path, value);
            setComments(root);
        }

        protected void setComments(DataKey root) {
            if (!SUPPORTS_SET_COMMENTS || !root.keyExists(path))
                return;
            ((MemoryDataKey) root).getSection("").setComments(path,
                    comments == null ? null : Arrays.asList(comments.split("<br>")));
        }
    }

    private static boolean SUPPORTS_SET_COMMENTS = true;
    static {
        try {
            ConfigurationSection.class.getMethod("getInlineComments", String.class);
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORTS_SET_COMMENTS = false;
        }
    }
}
