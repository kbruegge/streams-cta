package streams.cta.io;

import streams.cta.io.Event.FullEvent;
import streams.cta.io.MCShower.MCShower;

/**
 * EventIOData can be compared to AllHessData data type in hessioxxx Created by alexey on 24.06.15.
 */
public class EventIOData {
    MCShower mcShower;

    //TODO implement RunHeader as it is needed for event to be interpreted and read right
//    RunHeader run_header;

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
}
