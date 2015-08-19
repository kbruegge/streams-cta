// Generated by the protocol buffer compiler.  DO NOT EDIT!

package streams.cta.io.protobuf;

@SuppressWarnings("hiding")
public interface RawCTAEvent {

  public static final class RawEvent extends
      com.google.protobuf.nano.MessageNano {

    private static volatile RawEvent[] _emptyArray;
    public static RawEvent[] emptyArray() {
      // Lazily initializes the empty array
      if (_emptyArray == null) {
        synchronized (
            com.google.protobuf.nano.InternalNano.LAZY_INIT_LOCK) {
          if (_emptyArray == null) {
            _emptyArray = new RawEvent[0];
          }
        }
      }
      return _emptyArray;
    }

    // required string messageType = 1;
    public java.lang.String messageType;

    // required int32 telescope_id = 2;
    public int telescopeId;

    // required int32 roi = 3;
    public int roi;

    // required int32 num_pixel = 4;
    public int numPixel;

    // repeated int32 samples = 5 [packed = true];
    public int[] samples;

    public RawEvent() {
      clear();
    }

    public RawEvent clear() {
      messageType = "";
      telescopeId = 0;
      roi = 0;
      numPixel = 0;
      samples = com.google.protobuf.nano.WireFormatNano.EMPTY_INT_ARRAY;
      cachedSize = -1;
      return this;
    }

    @Override
    public void writeTo(com.google.protobuf.nano.CodedOutputByteBufferNano output)
        throws java.io.IOException {
      output.writeString(1, this.messageType);
      output.writeInt32(2, this.telescopeId);
      output.writeInt32(3, this.roi);
      output.writeInt32(4, this.numPixel);
      if (this.samples != null && this.samples.length > 0) {
        int dataSize = 0;
        for (int i = 0; i < this.samples.length; i++) {
          int element = this.samples[i];
          dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32SizeNoTag(element);
        }
        output.writeRawVarint32(42);
        output.writeRawVarint32(dataSize);
        for (int i = 0; i < this.samples.length; i++) {
          output.writeInt32NoTag(this.samples[i]);
        }
      }
      super.writeTo(output);
    }

    @Override
    protected int computeSerializedSize() {
      int size = super.computeSerializedSize();
      size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeStringSize(1, this.messageType);
      size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(2, this.telescopeId);
      size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(3, this.roi);
      size += com.google.protobuf.nano.CodedOutputByteBufferNano
          .computeInt32Size(4, this.numPixel);
      if (this.samples != null && this.samples.length > 0) {
        int dataSize = 0;
        for (int i = 0; i < this.samples.length; i++) {
          int element = this.samples[i];
          dataSize += com.google.protobuf.nano.CodedOutputByteBufferNano
              .computeInt32SizeNoTag(element);
        }
        size += dataSize;
        size += 1;
        size += com.google.protobuf.nano.CodedOutputByteBufferNano
            .computeRawVarint32Size(dataSize);
      }
      return size;
    }

    @Override
    public RawEvent mergeFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      while (true) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            return this;
          default: {
            if (!com.google.protobuf.nano.WireFormatNano.parseUnknownField(input, tag)) {
              return this;
            }
            break;
          }
          case 10: {
            this.messageType = input.readString();
            break;
          }
          case 16: {
            this.telescopeId = input.readInt32();
            break;
          }
          case 24: {
            this.roi = input.readInt32();
            break;
          }
          case 32: {
            this.numPixel = input.readInt32();
            break;
          }
          case 40: {
            int arrayLength = com.google.protobuf.nano.WireFormatNano
                .getRepeatedFieldArrayLength(input, 40);
            int i = this.samples == null ? 0 : this.samples.length;
            int[] newArray = new int[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.samples, 0, newArray, 0, i);
            }
            for (; i < newArray.length - 1; i++) {
              newArray[i] = input.readInt32();
              input.readTag();
            }
            // Last one without readTag.
            newArray[i] = input.readInt32();
            this.samples = newArray;
            break;
          }
          case 42: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            // First pass to compute array length.
            int arrayLength = 0;
            int startPos = input.getPosition();
            while (input.getBytesUntilLimit() > 0) {
              input.readInt32();
              arrayLength++;
            }
            input.rewindToPosition(startPos);
            int i = this.samples == null ? 0 : this.samples.length;
            int[] newArray = new int[i + arrayLength];
            if (i != 0) {
              java.lang.System.arraycopy(this.samples, 0, newArray, 0, i);
            }
            for (; i < newArray.length; i++) {
              newArray[i] = input.readInt32();
            }
            this.samples = newArray;
            input.popLimit(limit);
            break;
          }
        }
      }
    }

    public static RawEvent parseFrom(byte[] data)
        throws com.google.protobuf.nano.InvalidProtocolBufferNanoException {
      return com.google.protobuf.nano.MessageNano.mergeFrom(new RawEvent(), data);
    }

    public static RawEvent parseFrom(
            com.google.protobuf.nano.CodedInputByteBufferNano input)
        throws java.io.IOException {
      return new RawEvent().mergeFrom(input);
    }
  }
}
