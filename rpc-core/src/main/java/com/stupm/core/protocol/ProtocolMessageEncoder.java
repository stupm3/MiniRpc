package com.stupm.core.protocol;

import com.stupm.core.serializer.Serializer;
import com.stupm.core.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageEncoder {
    public static Buffer encode(ProtocolMessage<?> msg) throws IOException {
        if(msg == null || msg.getHeader() == null){
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = msg.getHeader();
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        ProtocolSerializerEnum enumByKey = ProtocolSerializerEnum.getEnumByKey(header.getSerializer());
        if(enumByKey == null){
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(enumByKey.getValue());
        byte[] serialize = serializer.serialize(msg.getBody());
        buffer.appendInt(serialize.length);
        buffer.appendBytes(serialize);
        return buffer;
    }
}
