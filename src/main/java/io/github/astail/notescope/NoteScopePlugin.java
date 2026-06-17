package io.github.astail.notescope;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NoteScopePlugin extends JavaPlugin {

    /** 視線チェックの実行間隔（tick）。4 tick ≒ 0.2 秒ごと。 */
    private static final long CHECK_PERIOD_TICKS = 4L;

    /** アクションバー表示を自分でオフにしたプレイヤー（サーバー稼働中のみ保持）。 */
    private final Set<UUID> disabled = ConcurrentHashMap.newKeySet();

    private BukkitTask lookTask;

    @Override
    public void onEnable() {
        if (!register("notescope", new NoteScopeCommand(this))) {
            return;
        }
        // 全オンラインプレイヤーの視線を定期判定するタスク。サーバー停止時は自動キャンセルされるが、
        // onDisable でも明示的に止める（リロード時の二重起動防止）。
        lookTask = new NoteLookTask(this).runTaskTimer(this, 0L, CHECK_PERIOD_TICKS);
        getLogger().info("NoteScope を有効化しました。音符ブロックを見るとアクションバーに音階が表示されます。");
    }

    @Override
    public void onDisable() {
        if (lookTask != null) {
            lookTask.cancel();
            lookTask = null;
        }
    }

    /** 指定プレイヤーがアクションバー表示を受け取るか（既定は ON）。 */
    public boolean isEnabledFor(UUID playerId) {
        return !disabled.contains(playerId);
    }

    /** 表示の ON/OFF を設定する。 */
    public void setEnabledFor(UUID playerId, boolean enabled) {
        if (enabled) {
            disabled.remove(playerId);
        } else {
            disabled.add(playerId);
        }
    }

    private boolean register(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("コマンド '" + name + "' が plugin.yml に未定義です。プラグインを無効化します。");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
        command.setExecutor(executor);
        if (executor instanceof TabCompleter completer) {
            command.setTabCompleter(completer);
        }
        return true;
    }
}
