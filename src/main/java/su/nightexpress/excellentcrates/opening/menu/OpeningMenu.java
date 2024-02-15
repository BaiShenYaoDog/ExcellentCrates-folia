package su.nightexpress.excellentcrates.opening.menu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentcrates.ExcellentCratesPlugin;
import su.nightexpress.excellentcrates.config.Config;
import su.nightexpress.excellentcrates.crate.impl.Crate;
import su.nightexpress.excellentcrates.opening.PlayerOpeningData;
import su.nightexpress.excellentcrates.opening.animation.AnimationInfo;
import su.nightexpress.excellentcrates.opening.animation.AnimationTask;
import su.nightexpress.excellentcrates.opening.slider.SliderInfo;
import su.nightexpress.excellentcrates.opening.slider.SliderTask;
import su.nightexpress.excellentcrates.opening.task.TaskStartAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpeningMenu extends ConfigMenu<ExcellentCratesPlugin> implements EventListener {

    private final Map<String, SliderInfo>    sliders;
    private final Map<String, AnimationInfo> animations;

    public OpeningMenu(@NotNull ExcellentCratesPlugin plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.sliders = new LinkedHashMap<>();
        this.animations = new LinkedHashMap<>();

        for (String sId : cfg.getSection("Rewards")) {
            String path2 = "Rewards." + sId + ".";

            TaskStartAction startAction = cfg.getEnum(path2 + "Start_Action", TaskStartAction.class, TaskStartAction.AUTO);
            double startChance = cfg.getDouble(path2 + "Start_Chance");
            long startDelay = cfg.getInt(path2 + "Start_Delay");
            int rollTimes = cfg.getInt(path2 + "Roll_Times");
            long rollTickInterval = cfg.getInt(path2 + "Roll_Tick_Interval");
            long rollSlowdownEvery = cfg.getInt(path2 + "Roll_Slowdown_Every");
            long rollSlowdownTicks = cfg.getInt(path2 + "Roll_Slowdown_Ticks");
            Sound soundTick = cfg.getEnum(path2 + "Tick_Sound", Sound.class);
            int[] slots = cfg.getIntArray(path2 + "Slots");
            SliderInfo.Mode mode = cfg.getEnum(path2 + "Slots_Mode", SliderInfo.Mode.class, SliderInfo.Mode.INDEPENDENT);
            int[] winSlots = cfg.getIntArray(path2 + "Win_Slots");

            SliderInfo info = new SliderInfo(sId, startAction, startChance, startDelay, rollTimes, rollTickInterval, rollSlowdownEvery, rollSlowdownTicks, soundTick, slots, mode, winSlots);
            this.sliders.put(info.getId(), info);
        }

        for (String sId : cfg.getSection("Animations")) {
            String path2 = "Animations." + sId + ".";

            TaskStartAction startAction = cfg.getEnum(path2 + "Start_Action", TaskStartAction.class, TaskStartAction.AUTO);
            long startDelay = cfg.getLong(path2 + "Start_Delay");
            int[] slots = cfg.getIntArray(path2 + "Slots");
            AnimationInfo.Mode mode = cfg.getEnum(path2 + "Mode", AnimationInfo.Mode.class, AnimationInfo.Mode.TOGETHER);
            int tickInterval = cfg.getInt(path2 + "Tick_Interval");
            Sound soundTick = cfg.getEnum(path2 + "Tick_Sound", Sound.class);

            List<ItemStack> items = new ArrayList<>();
            cfg.getSection(path2 + "Items").forEach(sId2 -> {
                items.add(cfg.getItem(path2 + "Items." + sId2));
            });

            AnimationInfo item = new AnimationInfo(sId, startAction, startDelay, slots, mode, tickInterval, soundTick, items);
            this.animations.put(item.getId(), item);
        }

        this.load();
        this.registerListeners();
    }

    @Override
    public void clear() {
        super.clear();
        this.unregisterListeners();
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    @NotNull
    protected MenuItem readItem(@NotNull String path) {
        CrateMenuItem menuItem = new CrateMenuItem(super.readItem(path));
        menuItem.setRewardId(cfg.getString(path + ".Start_Reward", ""));
        menuItem.setAnimationId(cfg.getString(path + ".Start_Animation", ""));
        menuItem.setClick((viewer, event) -> {
            Player player = viewer.getPlayer();
            PlayerOpeningData data = PlayerOpeningData.get(player);
            if (data == null) return;

            String[] rewardIds = menuItem.getRewardId();
            if (rewardIds != null) {
                for (String rewardId : rewardIds) {
                    SliderTask task = data.getSliderTasks().get(rewardId);
                    if (task == null || task.getParent().getStartAction() != TaskStartAction.CLICK) return;
                    if (task.isStarted()) return;
                    task.start();
                }
            }

            String[] animationIds = menuItem.getAnimationId();
            if (animationIds != null) {
                for (String animationId : animationIds) {
                    AnimationTask task = data.getAnimationTasks().get(animationId);
                    if (task == null || task.getParent().getStartAction() != TaskStartAction.CLICK) return;
                    if (task.isStarted()) return;
                    task.start();
                }
            }
        });
        return menuItem;
    }

    @Override
    public boolean open(@NotNull Player player, int page) {
        throw new IllegalStateException("Attempt to open crate opening GUI without the crate instance!");
    }

    public void open(@NotNull Player player, @NotNull Crate crate) {
        super.open(player, 1);

        Inventory inventory = player.getOpenInventory().getTopInventory(); // костыль !!!! (ну или нет)
        PlayerOpeningData data = PlayerOpeningData.create(player, crate, inventory);

        this.animations.values().forEach(animationInfo -> {
            AnimationTask task = new AnimationTask(data, animationInfo);
            if (animationInfo.getStartAction() == TaskStartAction.CLICK || task.start()) {
                data.getAnimationTasks().put(animationInfo.getId(), task);
            }
        });

        this.sliders.values().forEach(sliderInfo -> {
            SliderTask task = new SliderTask(data, sliderInfo);
            if (sliderInfo.getStartAction() == TaskStartAction.CLICK || task.start()) {
                data.getSliderTasks().put(sliderInfo.getId(), task);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInvClose14(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        PlayerOpeningData data = PlayerOpeningData.get(player);
        if (data == null) return;

        if (data.isActive()) {
            if (Config.CRATE_PREVENT_OPENING_SKIP.get() || !data.canSkip()) return;
            data.stop(false);
        }
        PlayerOpeningData.clean(player);
        this.plugin.getUserManager().saveUser(plugin.getUserManager().getUserData(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInvClick14(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        PlayerOpeningData data = PlayerOpeningData.get(player);
        if (data == null) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        PlayerOpeningData.clean(player);
    }

    static class CrateMenuItem extends MenuItem {

        private String[] rewardId;
        private String[] animationId;

        public CrateMenuItem(@NotNull MenuItem menuItem) {
            super(menuItem.getItem(), menuItem.getPriority(), menuItem.getOptions(), menuItem.getSlots());
        }

        public void setRewardId(@NotNull String rewardId) {
            this.rewardId = StringUtil.noSpace(rewardId).split(",");
        }

        @Nullable
        public String[] getRewardId() {
            return rewardId;
        }

        public void setAnimationId(@NotNull String animationId) {
            this.animationId = StringUtil.noSpace(animationId).split(",");
        }

        @Nullable
        public String[] getAnimationId() {
            return animationId;
        }
    }
}
