package streams.cta.io.eventio;

import streams.cta.io.eventio.event.FullEvent;
import streams.cta.io.eventio.mcshower.MCShower;
import streams.cta.io.eventio.runheader.RunHeader;

/**
 * EventIOData can be compared to AllHessData data type in hessioxxx
 *
 * @author alexey
 */
public class EventIOData {
    MCShower mcShower;

    RunHeader runHeader;

    /**
     * needed to read the event
     */
    FullEvent event;

//    MCEvent mc_event;
//    MCRunHeader mc_run_header;
//    CameraSettings camera_set[H_MAX_TEL];

    // TODO should be used later
//    CameraOrganisation camera_org[H_MAX_TEL];
//    PixelSetting pixel_set[H_MAX_TEL];
//    PixelDisabled pixel_disabled[H_MAX_TEL];
//    CameraSoftSet cam_soft_set[H_MAX_TEL];
//    TrackingSetup tracking_set[H_MAX_TEL];
//    PointingCorrection point_cor[H_MAX_TEL];
//    // MCpeSum mc_pesum;
//    TelMoniData tel_moni[H_MAX_TEL];
//    LasCalData tel_lascal[H_MAX_TEL];
//    RunStat run_stat;
//    MCRunStat mc_run_stat;

    public EventIOData() {
        // initialize run header object containing information about the EventIO run
        runHeader = new RunHeader();
        event = new FullEvent();
    }
}
