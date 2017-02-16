package streams.cta.io;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.LocalDateTime;


public class LocalDateTimeSerializer extends Serializer<LocalDateTime> {

    public LocalDateTimeSerializer() {
        setImmutable(true);
    }

    @Override
    public LocalDateTime read(Kryo kryo, Input input, Class<LocalDateTime> type) {
        final int year = input.readInt();
        final int month = input.readInt();
        final int day = input.readInt();
        final int hour = input.readInt();
        final int minute = input.readInt();
        final int second = input.readInt();
        final int nanoOfSecond = input.readInt();

        return LocalDateTime.of(year, month, day, hour, minute, second, nanoOfSecond);
    }

    @Override
    public void write(Kryo kryo, Output output, LocalDateTime localDateTime) {
        output.writeInt(localDateTime.getYear());
        output.writeInt(localDateTime.getMonthValue());
        output.writeInt(localDateTime.getDayOfMonth());
        output.writeInt(localDateTime.getHour());
        output.writeInt(localDateTime.getMinute());
        output.writeInt(localDateTime.getSecond());
        output.writeInt(localDateTime.getNano());
    }
}