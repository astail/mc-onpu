package io.github.astail.notescope;

import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Instrument;
import org.bukkit.Note;

/**
 * {@link Note} と {@link Instrument} を、人が読める音階表記のアクションバー用 Component に整形する。
 *
 * <p>音符ブロックの音は 0〜24 の 25 段階（F#3〜F#5 の 2 オクターブ）。
 * バニラでは設置直後が 0（F#3）で、右クリックごとに +1 され 24 で 0 に戻る。
 */
final class NoteFormatter {

    private NoteFormatter() {
    }

    /**
     * 例: 「♪ F#3 (ファ#)  調律 13/24  楽器: ハープ」
     *
     * @param note       音符ブロックの音（0〜24）
     * @param instrument 音符ブロックの楽器（真下のブロックで決まる）
     */
    static Component format(Note note, Instrument instrument) {
        int id = note.getId() & 0xFF; // byte → 0〜24
        return Component.text("♪ " + scientificName(note), NamedTextColor.GOLD)
                .append(Component.text(" (" + solfegeName(note) + ")", NamedTextColor.WHITE))
                .append(Component.text("  調律 " + id + "/24", NamedTextColor.GRAY))
                .append(Component.text("  楽器: " + instrumentName(instrument), NamedTextColor.AQUA));
    }

    /** 科学的音名（例: F#3, C4）。オクターブ番号は C で繰り上がるため id から算出する。 */
    private static String scientificName(Note note) {
        int id = note.getId() & 0xFF;
        int octave = 3 + (id + 6) / 12; // id 0(=F#3)〜5→3, 6(=C4)〜17→4, 18(=C5)〜24→5
        return letter(note) + (note.isSharped() ? "#" : "") + octave;
    }

    /** ドレミ表記（例: ファ#, ド）。 */
    private static String solfegeName(Note note) {
        String base = switch (note.getTone()) {
            case C -> "ド";
            case D -> "レ";
            case E -> "ミ";
            case F -> "ファ";
            case G -> "ソ";
            case A -> "ラ";
            case B -> "シ";
        };
        return base + (note.isSharped() ? "#" : "");
    }

    /** 音名のアルファベット表記。 */
    private static String letter(Note note) {
        return switch (note.getTone()) {
            case A -> "A";
            case B -> "B";
            case C -> "C";
            case D -> "D";
            case E -> "E";
            case F -> "F";
            case G -> "G";
        };
    }

    /** 楽器名（日本語）。未知の楽器は enum 名を見やすい形へフォールバック。 */
    private static String instrumentName(Instrument instrument) {
        return switch (instrument) {
            case PIANO -> "ハープ";
            case BASS_DRUM -> "バスドラム";
            case SNARE_DRUM -> "スネアドラム";
            case STICKS -> "スティック";
            case BASS_GUITAR -> "ベース";
            case FLUTE -> "フルート";
            case BELL -> "ベル";
            case GUITAR -> "ギター";
            case CHIME -> "チャイム";
            case XYLOPHONE -> "シロフォン";
            case IRON_XYLOPHONE -> "アイアンシロフォン";
            case COW_BELL -> "カウベル";
            case DIDGERIDOO -> "ディジュリドゥ";
            case BIT -> "ビット";
            case BANJO -> "バンジョー";
            case PLING -> "プリング";
            default -> prettify(instrument.name()); // 例: WITHER_SKELETON → Wither Skeleton
        };
    }

    private static String prettify(String enumName) {
        String[] parts = enumName.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
