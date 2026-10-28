package util;

import java.awt.Color;
import java.awt.Font;

public final class GameConfig {
    public static final String GAME_TITLE = "MELLOW: Emergency Dispatch Command";
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int TOP_BAR_HEIGHT = 60;
    public static final int BOTTOM_BAR_HEIGHT = 120;
    public static final int FPS = 60;
    public static final int FRAME_DELAY_MS = 1000 / FPS;

    public static final double SHIFT_DURATION_SECONDS = 180.0;
    public static final int TOTAL_SHIFTS = 3;

    public static double DEADLINE_SCALE = 1.15;
    public static double CALL_FREQUENCY_SCALE = 1.0;
    public static double PANIC_SCALE = 1.0;
    public static boolean BLOCKED_ROAD_AS_PENALTY_FALLBACK = false;
    public static double BLOCKED_ROAD_PENALTY_SECONDS = 18.0;

    public static final double BASE_SPAWN_INTERVAL_SHIFT_1 = 22.0;
    public static final double BASE_SPAWN_INTERVAL_SHIFT_2 = 16.0;
    public static final double BASE_SPAWN_INTERVAL_SHIFT_3 = 12.5;

    public static final int MAX_SIMULTANEOUS_SHIFT_1 = 3;
    public static final int MAX_SIMULTANEOUS_SHIFT_2 = 5;
    public static final int MAX_SIMULTANEOUS_SHIFT_3 = 7;

    public static final double CALM_CALLER_DURATION = 3.2;
    public static final double CALM_PANIC_REDUCTION = 28.0;
    public static final double CALM_DEADLINE_BONUS = 9.0;

    public static final double BREATHING_DURATION = 9.0;
    public static final double BREATHING_CYCLE_SECONDS = 3.0;
    public static final double BREATHING_MAX_STRESS_RELIEF = 38.0;

    public static final double MAX_STRESS = 100.0;
    public static final double MAX_PANIC = 100.0;
    public static final double MAX_FATIGUE = 100.0;
    public static final double FATIGUE_REST_THRESHOLD = 82.0;
    public static final double FATIGUE_RECOVERY_PER_SECOND = 7.5;

    public static final String SAVE_FILE_PATH = "rescue_radio_save.txt";

    public static final Color BG_DEEP_NIGHT = new Color(11, 18, 32);
    public static final Color BG_PANEL = new Color(18, 29, 49);
    public static final Color BG_PANEL_ALT = new Color(25, 39, 64);
    public static final Color BG_CARD = new Color(29, 44, 72);
    public static final Color BG_CARD_HOVER = new Color(37, 56, 90);
    public static final Color BG_CARD_SELECTED = new Color(46, 70, 112);
    public static final Color LAMP_YELLOW = new Color(247, 197, 72);
    public static final Color JEEPNEY_ORANGE = new Color(242, 118, 46);
    public static final Color JEEPNEY_RED = new Color(228, 73, 73);
    public static final Color JEEPNEY_TEAL = new Color(46, 196, 182);
    public static final Color JEEPNEY_GREEN = new Color(82, 196, 118);
    public static final Color JEEPNEY_BLUE = new Color(78, 168, 222);
    public static final Color TEXT_PRIMARY = new Color(242, 238, 228);
    public static final Color TEXT_MUTED = new Color(165, 179, 204);
    public static final Color BORDER_SUBTLE = new Color(56, 77, 115);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_MONO = new Font("Monospaced", Font.BOLD, 13);

    private GameConfig() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
