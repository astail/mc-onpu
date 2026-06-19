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

    /**
     * 各列を揃えるための目標幅（px）。取りうる全ての音・楽器を走査して最大幅を求める。
     * 各列をこの幅まで右詰めパディングすることで、後続項目の開始位置と行の総幅が一定になり、
     * 「♪」「調律」「楽器:」の表示位置がずれなくなる（音名・楽器名が変わっても固定）。
     */
    private static final int COL_NOTE_WIDTH;   // 「♪ 音名 (ドレミ)」
    private static final int COL_TUNING_WIDTH; // 「  調律 n/24」
    private static final int COL_INSTRUMENT_WIDTH; // 「  楽器: 名前」

    static {
        int noteW = 0;
        int tuningW = 0;
        int instrumentW = 0;
        for (int id = 0; id <= 24; id++) {
            Note note = new Note(id);
            noteW = Math.max(noteW, FontWidth.width("♪ " + scientificName(note) + " (" + solfegeName(note) + ")"));
            tuningW = Math.max(tuningW, FontWidth.width("  調律 " + id + "/24"));
        }
        for (Instrument instrument : Instrument.values()) {
            instrumentW = Math.max(instrumentW, FontWidth.width("  楽器: " + instrumentName(instrument)));
        }
        COL_NOTE_WIDTH = noteW;
        COL_TUNING_WIDTH = tuningW;
        COL_INSTRUMENT_WIDTH = instrumentW;
    }

    private NoteFormatter() {
    }

    /**
     * 例: 「♪ F#3 (ファ#)  調律 13/24  楽器: ハープ」
     *
     * <p>各列を固定幅まで半角スペースでパディングし、視点を移しても「♪」「調律」「楽器:」が
     * 横にずれないようにする。パディングの不可視スペースは各色の成分末尾に付与する。
     *
     * @param note       音符ブロックの音（0〜24）
     * @param instrument 音符ブロックの楽器（真下のブロックで決まる）
     */
    static Component format(Note note, Instrument instrument) {
        int id = note.getId() & 0xFF; // byte → 0〜24
        String head = "♪ " + scientificName(note);          // GOLD
        String solfege = " (" + solfegeName(note) + ")";     // WHITE
        String tuning = "  調律 " + id + "/24";               // GRAY
        String instrumentText = "  楽器: " + instrumentName(instrument); // AQUA

        // ドレミ成分の末尾を詰めて「♪ 音名 (ドレミ)」全体を固定幅にする → 調律の開始位置が固定。
        String solfegePadded = FontWidth.padRight(solfege, COL_NOTE_WIDTH - FontWidth.width(head));
        // 調律列を固定幅にする → 楽器: の開始位置が固定。
        String tuningPadded = FontWidth.padRight(tuning, COL_TUNING_WIDTH);
        // 楽器列を固定幅にする → 行の総幅が一定 → 中央寄せが一定 → 行頭の ♪ も固定。
        String instrumentPadded = FontWidth.padRight(instrumentText, COL_INSTRUMENT_WIDTH);

        return Component.text(head, NamedTextColor.GOLD)
                .append(Component.text(solfegePadded, NamedTextColor.WHITE))
                .append(Component.text(tuningPadded, NamedTextColor.GRAY))
                .append(Component.text(instrumentPadded, NamedTextColor.AQUA));
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
