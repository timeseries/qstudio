package com.timestored.sqldash.chart;


import java.awt.Color;

/**
 * Class that jfree chart uses to select its colors. 
 */
class JFreeChartColor extends Color {

	private static final long serialVersionUID = 1L;

	/** A very dark red color. */
    public static final Color VERY_DARK_RED = new Color(0x80, 0x00, 0x00);

    /** A dark red color. */
    public static final Color DARK_RED = new Color(0xc0, 0x00, 0x00);

    /** A light red color. */
    public static final Color LIGHT_RED = new Color(0xFF, 0x40, 0x40);

    /** A very light red color. */
    public static final Color VERY_LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    /** A very dark yellow color. */
    public static final Color VERY_DARK_YELLOW = new Color(0x80, 0x80, 0x00);

    /** A dark yellow color. */
    public static final Color DARK_YELLOW = new Color(0xC0, 0xC0, 0x00);

    /** A light yellow color. */
    public static final Color LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x40);

    /** A very light yellow color. */
    public static final Color VERY_LIGHT_YELLOW = new Color(0xFF, 0xFF, 0x80);

    /** A very dark green color. */
    public static final Color VERY_DARK_GREEN = new Color(0x00, 0x80, 0x00);

    /** A dark green color. */
    public static final Color DARK_GREEN = new Color(0x00, 0xC0, 0x00);

    /** A light green color. */
    public static final Color LIGHT_GREEN = new Color(0x40, 0xFF, 0x40);

    /** A very light green color. */
    public static final Color VERY_LIGHT_GREEN = new Color(0x80, 0xFF, 0x80);

    /** A very dark cyan color. */
    public static final Color VERY_DARK_CYAN = new Color(0x00, 0x80, 0x80);

    /** A dark cyan color. */
    public static final Color DARK_CYAN = new Color(0x00, 0xC0, 0xC0);

    /** A light cyan color. */
    public static final Color LIGHT_CYAN = new Color(0x40, 0xFF, 0xFF);

    /** Aa very light cyan color. */
    public static final Color VERY_LIGHT_CYAN = new Color(0x80, 0xFF, 0xFF);

    /** A very dark blue color. */
    public static final Color VERY_DARK_BLUE = new Color(0x00, 0x00, 0x80);

    /** A dark blue color. */
    public static final Color DARK_BLUE = new Color(0x00, 0x00, 0xC0);

    /** A light blue color. */
    public static final Color LIGHT_BLUE = new Color(0x40, 0x40, 0xFF);

    /** A very light blue color. */
    public static final Color VERY_LIGHT_BLUE = new Color(0x80, 0x80, 0xFF);

    /** A very dark magenta/purple color. */
    public static final Color VERY_DARK_MAGENTA = new Color(0x80, 0x00, 0x80);

    /** A dark magenta color. */
    public static final Color DARK_MAGENTA = new Color(0xC0, 0x00, 0xC0);

    /** A light magenta color. */
    public static final Color LIGHT_MAGENTA = new Color(0xFF, 0x40, 0xFF);

    /** A very light magenta color. */
    public static final Color VERY_LIGHT_MAGENTA = new Color(0xFF, 0x80, 0xFF);

    /**
     * Creates a Color with an opaque sRGB with red, green and blue values in
     * range 0-255.
     *
     * @param r  the red component in range 0x00-0xFF.
     * @param g  the green component in range 0x00-0xFF.
     * @param b  the blue component in range 0x00-0xFF.
     */
    public JFreeChartColor(int r, int g, int b) {
        super(r, g, b);
    }

    /**
     * Convenience method to return an array of <code>Paint</code> objects that
     * represent the pre-defined colors in the <code>Color</code> and
     * <code>ChartColor</code> objects.
     *
     * @return An array of objects with the <code>Paint</code> interface.
     */
    public static Color[] createDefaultColorArray() {

        return new Color[] {
            new Color(0xFF, 0x55, 0x55),
            new Color(0x55, 0x55, 0xFF),
            new Color(0x55, 0xFF, 0x55),
//            new Color(0xFF, 0xFF, 0x55), // remove yellow. Was too light!
            new Color(0xFF, 0x55, 0xFF),
            new Color(0x55, 0xFF, 0xFF),
            Color.pink,
            Color.gray,
            DARK_RED,
            DARK_BLUE,
            DARK_GREEN,
            DARK_YELLOW,
            DARK_MAGENTA,
            DARK_CYAN,
            Color.darkGray,
            LIGHT_RED,
            LIGHT_BLUE,
            LIGHT_GREEN,
            LIGHT_YELLOW,
            LIGHT_MAGENTA,
            LIGHT_CYAN,
            Color.lightGray,
            VERY_DARK_RED,
            VERY_DARK_BLUE,
            VERY_DARK_GREEN,
            VERY_DARK_YELLOW,
            VERY_DARK_MAGENTA,
            VERY_DARK_CYAN,
            VERY_LIGHT_RED,
            VERY_LIGHT_BLUE,
            VERY_LIGHT_GREEN,
            VERY_LIGHT_YELLOW,
            VERY_LIGHT_MAGENTA,
            VERY_LIGHT_CYAN
        };
    }

}