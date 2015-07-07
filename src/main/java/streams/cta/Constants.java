package streams.cta;

/**
 * Created by alexey on 17.06.15.
 */
public class Constants {

    /**
     * The max. number of MC shower profiles.
     */
    public static final int H_MAX_PROFILE = 10;
    public static final int MAX_IO_ITEM_LEVEL = 20;
    public static final int H_MAX_TEL = 16;
    public static final int H_MAX_PIX = 4095;
    public static final int H_MAX_TRG_PER_SECTOR = 1;
    public static final int H_MAX_SECTORS = H_MAX_PIX * H_MAX_TRG_PER_SECTOR;

    public static final int MAX_TEL_TRIGGERS = 3;

    /**
     * Maximum number of different gains per PM
     */
    public static final int H_MAX_GAINS = 2;

    /**
     * Maximum number of time slices handled.
     */
    public static final int H_MAX_SLICES = 128;

    public static final int H_MAX_PIX_TIMES = 7;

    /**
     * The max. size of the list of hottest pix.
     */
    public static final int H_MAX_HOTPIX = 5;

    public static final int TYPE_CENTRAL_EVENT = 2009;
    public static final int TYPE_TRACK_EVENT = 2100;
    public static final int TYPE_TEL_EVENT = 2200;
    public static final int TYPE_SHOWER = 2015;

}
