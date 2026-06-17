package io.github.astail.notescope;

import org.bukkit.FluidCollisionMode;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

/**
 * オンラインプレイヤーの視線を定期的に判定し、見ている先が音符ブロックなら
 * その音階情報をアクションバーへ表示するタスク。
 */
final class NoteLookTask extends BukkitRunnable {

    /** 視線判定の最大距離（ブロック）。クリエイティブの届く距離を少し超える程度。 */
    private static final double REACH = 6.0D;

    private final NoteScopePlugin plugin;

    NoteLookTask(NoteScopePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission("notescope.use")) {
                continue;
            }
            if (!plugin.isEnabledFor(player.getUniqueId())) {
                continue;
            }
            NoteBlock noteBlock = noteBlockInSight(player);
            if (noteBlock == null) {
                // 何も見ていない / 音符ブロック以外。アクションバーは数秒で自然に消えるため放置でよい。
                continue;
            }
            player.sendActionBar(NoteFormatter.format(noteBlock.getNote(), noteBlock.getInstrument()));
        }
    }

    /** プレイヤーが見ている先の音符ブロックを返す。なければ null。 */
    private NoteBlock noteBlockInSight(Player player) {
        // 目線からの視線レイ。通り抜け可能ブロック（草など）は無視し、最初の当たるブロックを取得。
        RayTraceResult result = player.rayTraceBlocks(REACH, FluidCollisionMode.NEVER);
        if (result == null) {
            return null;
        }
        Block block = result.getHitBlock();
        if (block == null) {
            return null;
        }
        BlockData data = block.getBlockData();
        return (data instanceof NoteBlock noteBlock) ? noteBlock : null;
    }
}
