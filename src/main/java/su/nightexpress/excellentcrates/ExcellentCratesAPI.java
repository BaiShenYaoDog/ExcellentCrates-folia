package su.nightexpress.excellentcrates;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.crate.CrateManager;
import su.nightexpress.excellentcrates.data.UserManager;
import su.nightexpress.excellentcrates.data.impl.CrateUser;
import su.nightexpress.excellentcrates.key.KeyManager;
import su.nightexpress.excellentcrates.menu.MenuManager;

public class ExcellentCratesAPI {

    public static final ExcellentCratesPlugin PLUGIN = ExcellentCratesPlugin.getPlugin(ExcellentCratesPlugin.class);

    @NotNull
    public static CrateUser getUserData(@NotNull Player player) {
        return PLUGIN.getUserManager().getUserData(player);
    }

    @NotNull
    public static UserManager getUserManager() {
        return PLUGIN.getUserManager();
    }

    @NotNull
    public static CrateManager getCrateManager() {
        return PLUGIN.getCrateManager();
    }

    @NotNull
    public static KeyManager getKeyManager() {
        return PLUGIN.getKeyManager();
    }

    @NotNull
    public static MenuManager getMenuManager() {
        return PLUGIN.getMenuManager();
    }
}
