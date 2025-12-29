package org.springframework.data.remote.vertx;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Generic Protobuf message codec for Vert.x event bus.
 * Handles serialization and deserialization of Protocol Buffer messages.
 *
 * @param <T> the Protobuf message type
 */
public class ProtobufMessageCodec<T extends Message> implements MessageCodec<T, T> {

    private final Class<T> messageClass;
    private final String codecName;

    public ProtobufMessageCodec(Class<T> messageClass, String codecName) {
        this.messageClass = messageClass;
        this.codecName = codecName;
    }

    @Override
    public void encodeToWire(Buffer buffer, T message) {
        byte[] bytes = message.toByteArray();
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        int start = pos + 4;
        byte[] bytes = buffer.getBytes(start, start + length);
        
        try {
            // Use reflection to call parseFrom method
            java.lang.reflect.Method parseFrom = messageClass.getMethod("parseFrom", byte[].class);
            return (T) parseFrom.invoke(null, bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode protobuf message", e);
        }
    }

    @Override
    public T transform(T message) {
        // For local delivery, no transformation needed
        return message;
    }

    @Override
    public String name() {
        return codecName;
    }

    @Override
    public byte systemCodecID() {
        // -1 means user codec
        return -1;
    }
}
