/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.constants;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GuiConstants {
    // App
    public final static String APP_TITLE = "Visual Odometry GUI";
    public final static String APP_TITLE_PATTERN = APP_TITLE + " %s";
    public final static String ABOUT_TITLE = "About";
    public final static String APP_DESCRIPTION = "Tracking and Mapping System based on Visual Odometry";
    public final static String AUTHOR_INFO = "Marco Trinastich ©%s";
    public final static String JAVA_INFO = "Running on java %s / quarkus 3.x";
    public final static String BOOFCV_INFO = "Based on BoofCv %s (%s)";
    public final static String LICENSE_INFO = "Released under Apache 2.0 license";


    // Video frames
    public final static String INPUT_VIDEO_FRAME_TITLE = "Video Input";
    public final static String INPUT_VIDEO_FRAME_TITLE_PATTERN = "Video Input: %sx%s / Preview: %sx%s%s";
    public final static String OUTPUT_VIDEO_FRAME_TITLE = "VO Processing";
    public final static String OUTPUT_VIDEO_FRAME_TITLE_PATTERN = "VO Processing: %sx%s%s / Preview: %sx%s%s";
    public final static String VIDEO_FULL_RESOLUTION = " (full resolution)";
    public final static String VIDEO_RESIZED = " (resized)";


    // Info frame
    public final static String INFO_FRAME_TITLE = "Info panel";
    public final static String CHART_XZ_TITLE = "X/Z";
    public final static String CHART_Y_TITLE = "Y";
    public final static String CHART_START = "Chart %d";
    public final static String INFO_PANEL_TITLE = "Info";
    public final static int INFO_PANEL_WIDTH = 400;
    public final static int INFO_PANEL_HEIGHT = 845;
    public final static String LBL_STATUS = "<html><b>Status:</b> %s</html>";
    public final static String LIST_CHART_START = CHART_START;
    public final static String LIST_CHART_END = "End Chart %d";
    public final static String LIST_CHART_POINT = "Frame: %d, El. Time: %s, Location X: %s, Y: %s, Z: %s, " +
            "inliers: %s%%";
    public final static String LIST_CHART_POINT_DETAILS = ", (Chart Type %s)";
    public final static String LIST_CHART_NA = "n/a";
    public final static String LBL_INFO = "Processing info:";
    public final static String LBL_CALIBRATION_FILE = "Calibration file: ";
    public final static String LBL_PROCESSED_VIDEO = "Processed video: ";
    public final static String LBL_PROCESSED_DEVICE = "Processed device: ";
    public final static String LBL_PROCESSED_FRAME = "Processed frames: ";
    public final static String LBL_PROCESSED_FRAME_TEXT = "%s (%s skipped)";
    public final static String LBL_ELAPSED_TIME = "Elapsed time: ";
    public final static String LBL_POSITIONS = "Current position (Translation):";
    public final static String LBL_X_POSITION = "X: ";
    public final static String LBL_Y_POSITION = "Y: ";
    public final static String LBL_Z_POSITION = "Z: ";
    public final static String LBL_XZ_DISTANCE = "Distance covered (X/Z): ";
    public final static String LBL_Y_DISTANCE = "Altitude covered (Y): ";
    public final static int DIRECTION_PANEL_WIDTH = 70;
    public final static int DIRECTION_PANEL_HEIGHT = 70;
    public final static float DIRECTION_PANEL_BORDER = 0.2f;
    public final static String ROTATION_PANEL_TOOLTIP = "Rotation: %s° %s (sin: %s, cos: %s)";
    public final static String ROTATION_CW = "(clockwise)";
    public final static String ROTATION_CCW = "(counter-clockwise)";
    public final static String ALTITUDE_PANEL_TOOLTIP = "Altitude delta: %s %s";
    public final static String ALTITUDE_PANEL_INC = "(increment)";
    public final static String ALTITUDE_PANEL_DEC = "(decrement)";
    public final static String LBL_ROTATION = "Rotation Matrix:";
    public final static String LBL_ROTATION_HEADER = "Type = %s, rows = %d, cols = %d";
    public final static String LBL_TRACKING_INFO = "Tracking info:";
    public final static String LBL_TRACKING_STATUS = "Total tracked features: ";
    public final static String LBL_TRACKING_STATUS_TEXT = "%s (inliers: %s, new tracks: %s)";
    public final static String LBL_TRACKING_INLIERS = "Inliers (matches): ";
    public final static String LBL_INPUT_FPS = "Input source framerate:";
    public final static String LBL_OUTPUT_FPS = "Visual odometry framerate:";
    public final static String LBL_CURRENT_FPS = "Current FPS: ";
    public final static String LBL_AVERAGE_FPS = "Average FPS: ";
    public final static String LBL_FPS_TEXT = "%s fps";
    public final static String LBL_BUFFER_INFO = "Buffer Load:";
    public final static String LBL_BUFFER_HINT = "%s/%s ";
    public final static String LBL_BUFFER_INFINITE = "Inf.";
    public final static String LBL_BUFFER_STABLE = "Buffer stable";
    public final static String LBL_BUFFER_OVER_RUN = "Buffer overrun";
    public final static String LBL_BUFFER_UNAVAILABLE = "Buffer unavailable";
    public final static String LBL_TRACKED_POINTS = "Found Points (Log):";


    // Control panel frame
    public final static String MAIN_FRAME_TITLE = "Control panel";
    // Toolbar
    public final static String MNU_LOAD_SETTINGS_TEXT = "Load settings";
    public final static String MNU_SAVE_SETTINGS_TEXT = "Save settings";
    public final static String MNU_RESET_SETTINGS_TEXT = "Reset settings to defaults";
    public final static String MNU_SWITCH_SETTINGS_TEXT = "Choose save format...";
    public final static String BTN_START_TOOLTIP = "Start";
    public final static String BTN_PAUSE_TOOLTIP = "Pause/Resume";
    public final static String BTN_RESET_TOOLTIP = "Reset Visual Odometry";
    public final static String BTN_STOP_TOOLTIP = "Stop";
    public final static String BTN_CLEAR_TOOLTIP = "Clear";
    public final static String BTN_TIMED_PROCESSING_VO_TOOLTIP = "Timed processing";
    public final static String DLG_TIMED_PROCESSING_TITLE = "Timed capture";
    public final static String DLG_TIMED_PROCESSING_MESSAGE = "Please input a timeout in seconds:";
    public final static String DLG_TIMED_PROCESSING_DEFAULT_VALUE = "60";

    // Visual Odometry Panel

    // Mono Visual Odometry
    public final static String LBL_VO_MONO_RANSAC_ITERATIONS = "Ransac iterations:";
    public final static String LBL_VO_MONO_THRESHOLD_RETIRE = "Threshold retire:";

    // MonoPlaneInfinity
    public final static String LBL_VO_MONO_INF_THRESHOLD_ADD = "Threshold add:";
    public final static String LBL_VO_MONO_INF_INLIER_PIXEL_TOL = "Inlier pixel tolerance:";

    // MonoPlaneOverhead
    public final static String LBL_VO_MONO_OVH_CELL_SIZE = "Cell size:";
    public final static String LBL_VO_MONO_OVH_MAX_CELL_PER_PIXEL = "Max cells per pixel:";
    public final static String LBL_VO_MONO_OVH_MAP_HEIGHT_FRACTION = "Map height fraction:";
    public final static String LBL_VO_MONO_OVH_INLIER_GROUND_TOL = "Inlier ground tolerance:";
    public final static String LBL_VO_MONO_OVH_ABSOLUTE_MINIMUM_TRACKS = "Absolute minimum tracks:";
    public final static String LBL_VO_MONO_OVH_RESPAWN_TRACK_FRACTION = "Respawn track fraction:";
    public final static String LBL_VO_MONO_OVH_RESPAWN_COVERAGE_FRACTION = "Respawn coverage fraction:";

    // Generic
    public final static String PERCENTAGE_TEXT = "%s%%";
    public final static String ON = "on";
    public final static String TRUE = "true";
    public final static String COMBO_BOX_CHECKMARK = "✓";
    public final static String COMBO_BOX_DEFAULT_SPACE = "      ";
    public final static String COMBO_BOX_CENTERED_SPACE = "    ";
    public final static String COMBO_BOX_NO_PREFIX_SPACE = " ";
    public final static String COMBO_BOX_FALLBACK_PREFIX = "%s  ".formatted(COMBO_BOX_CHECKMARK);
    public final static String COMBO_BOX_FALLBACK_SPACE = "     ";


    // Html tags
    public final static String BOLD_TAG = "<b>%s</b>";
    public final static String HTML_TAG = "<html>%s</html>";


    // Native properties
    public final static String AWT_AA_FONT = "awt.useSystemAAFontSettings";
    public final static String AWT_DESKTOP_HINTS = "awt.font.desktophints";
    public final static String SWING_AA_TEXT = "swing.aatext";
    public final static String JAVA2D_X_RENDER = "sun.java2d.xrender";
    public final static String MACOS_APP_TITLE_PROPERTY = "apple.awt.application.name";
    public final static String MACOS_TRANSPARENT_TITLE_PROPERTY = "apple.awt.transparentTitleBar";
    public final static String MACOS_APP_APPEARANCE_PROPERTY = "apple.awt.application.appearance";
    public final static String MACOS_APP_APPEARANCE_VALUE = "system";
    public final static String JAVA_VERSION = "java.version";


    // LookAndFeel properties
    public final static String LABEL_FONT_PROP = "Label.font";
    public final static String ALL_BACKGROUNDS_PROP = ".background";
    public final static String COMBO_BOX_BACKGROUND_PROP = "ComboBox.background";
    public final static String LIST_BACKGROUND_PROP = "List.background";
    public final static String LIST_SELECTION_BACKGROUND_PROP = "List.selectionBackground";
    public final static String TEXT_FIELD_BACKGROUND_PROP = "TextField.background";
    public final static String TEXT_FIELD_BORDER_PROP = "TextField.border";


    // Fonts
    public final static Font DEFAULT_FONT = UIManager.getFont(LABEL_FONT_PROP);
    public final static Font BOLD_FONT = new Font(DEFAULT_FONT.getFontName(), Font.BOLD, DEFAULT_FONT.getSize());


    // Colors
    public final static Color ALMOST_WHITE = new Color(246, 248, 252, 255);
    public final static Color LIGHT_BLUE = new Color(55, 118, 171, 204);
    public final static Color AQUA_BLUE = new Color(16, 109, 218, 255);
    public final static Color DARK_BLUE = new Color(23, 72, 108, 204);
    public final static Color LIGHT_GREEN = new Color(107, 171, 55, 204);
    public final static Color LIGHT_RED = new Color(205, 45, 45, 204);
    public final static Color LIGHT_BLACK = new Color(10, 10, 10, 30);

    public final static Color PANEL_BORDER_ACTIVE_COLOR = DARK_BLUE;
    public final static Color PANEL_BORDER_INACTIVE_COLOR = LIGHT_BLACK;
    public final static Color COMBO_BOX_BACKGROUND_COLOR = Color.WHITE;
    public final static Color LIST_BACKGROUND_COLOR = Color.WHITE;
    public final static Color LIST_FOREGROUND_COLOR = Color.BLACK;
    public final static Color LIST_SELECTION_BACKGROUND_COLOR = AQUA_BLUE;
    public final static Color LIST_SELECTION_FOREGROUND_COLOR = Color.WHITE;
    public final static Color TEXT_FIELD_BACKGROUND_COLOR = Color.WHITE;
    public final static Color TEXT_FIELD_BORDER_BASE_COLOR = GuiConstants.LIGHT_BLACK;
    public final static Color TEXT_FIELD_BORDER_HIGHLIGHT_COLOR = GuiConstants.LIGHT_BLUE;
    public final static Color APP_BACKGROUND_COLOR = ALMOST_WHITE;


    // Icons
    public final static String APP_ICON = "/gui/icons/app_logo.png";
    public final static String BTN_SETTINGS = "/gui/icons/btn_settings.png";
    public final static String BTN_START = "/gui/icons/btn_play.png";
    public final static String BTN_START_DISABLED = "/gui/icons/btn_play_disabled.png";
    public final static String BTN_PAUSE = "/gui/icons/btn_pause.png";
    public final static String BTN_PAUSE_DISABLED = "/gui/icons/btn_pause_disabled.png";
    public final static String BTN_RESET = "/gui/icons/btn_reset.png";
    public final static String BTN_STOP = "/gui/icons/btn_stop.png";
    public final static String BTN_STOP_DISABLED = "/gui/icons/btn_stop_disabled.png";
    public final static String BTN_CLEAR = "/gui/icons/btn_clear.png";
    public final static String BTN_TIMED_PROCESSING_VO = "/gui/icons/btn_camera_timer.png";
    public final static String BTN_EMPTY = "/gui/icons/btn_empty.png";


    // Images/Resolutions
    public final static List<Integer> HI_RES_VARIANTS = Arrays.asList(32, 64, 128, 256);
    public final static int HI_RES_SIZE = HI_RES_VARIANTS.getLast();
}
