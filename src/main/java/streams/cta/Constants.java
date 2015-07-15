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

    public static final int TYPE_BASE = 2000;
    public static final int TYPE_CENTRAL_EVENT = 2009;
    public static final int TYPE_TRACK_EVENT = 2100;
    public static final int TYPE_TEL_EVENT = 2200;
    public static final int TYPE_TELADCSUM = TYPE_BASE + 12;
    public static final int TYPE_TELADCSAMP = TYPE_BASE + 13;
    public static final int TYPE_TELIMAGE = TYPE_BASE + 14;
    public static final int TYPE_SHOWER = TYPE_BASE + 15;
    public static final int TYPE_PIXELTIMING = TYPE_BASE + 16;
    public static final int TYPE_PIXELCALIB = TYPE_BASE + 17;
    public static final int TYPE_PIXELLIST = TYPE_BASE + 27;

    public static final int H_MAX_DRAWERS = H_MAX_PIX;

    public static final int RAWDATA_FLAG = 0x01;
    public static final int RAWSUM_FLAG = 0x02;

    public static final int TIME_FLAG = 0x200;
    public static final int IMG_BASE_FLAG = 0x10;
    public static final int IMG_ERR_FLAG = 0x20;
    public static final int IMG_34M_FLAG = 0x40;
    public static final int IMG_HOT_FLAG = 0x80;
    public static final int IMG_PIXTM_FLAG = 0x100;
    public static final int IMAGE_FLAG = IMG_BASE_FLAG | IMG_ERR_FLAG | IMG_34M_FLAG | IMG_HOT_FLAG | IMG_PIXTM_FLAG;

    // Index to low-gain channels in adc_sum, adc_sample, pedestal, ...
    public static final int LO_GAIN = 1;

    //< Index to high-gain channels in adc_sum, adc_sample, pedestal, ...
    public static final int HI_GAIN = 0;

}
