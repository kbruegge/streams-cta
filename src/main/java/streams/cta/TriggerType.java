package streams.cta;

/**
 * Created by Kai on 27.07.15.
 */
public enum TriggerType {
    TRIGGER(0),
    PEDESTAL(1);

    TriggerType(int type) {
        this.type = type;
    }

    int type;
}
