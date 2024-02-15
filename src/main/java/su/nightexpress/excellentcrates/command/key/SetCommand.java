package su.nightexpress.excellentcrates.command.key;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentcrates.ExcellentCratesPlugin;
import su.nightexpress.excellentcrates.config.Perms;
import su.nightexpress.excellentcrates.config.Lang;
import su.nightexpress.excellentcrates.data.impl.CrateUser;
import su.nightexpress.excellentcrates.key.CrateKey;

class SetCommand extends ManageCommand {

    public SetCommand(@NotNull ExcellentCratesPlugin plugin) {
        super(plugin, new String[]{"set"}, Perms.COMMAND_KEY_SET);
        this.setDescription(plugin.getMessage(Lang.COMMAND_KEY_SET_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_KEY_SET_USAGE));
        this.setMessageNotify(plugin.getMessage(Lang.COMMAND_KEY_SET_NOTIFY));
        this.setMessageDone(plugin.getMessage(Lang.COMMAND_KEY_SET_DONE));
    }

    @Override
    protected void manage(@NotNull CrateUser user, @NotNull CrateKey key, int amount) {
        this.plugin.getKeyManager().setKey(user, key, amount);
    }
}
