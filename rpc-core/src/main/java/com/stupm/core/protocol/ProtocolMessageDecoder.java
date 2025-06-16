package com.stupm.core.protocol;

import com.stupm.core.constant.ProtocolConstants;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.serializer.Serializer;
import com.stupm.core.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageDecoder {
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        if(magic != ProtocolConstants.PROTOCOL_MAGIC){
            throw new IOException("Invalid magic number");
        }
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        byte[] bytes = buffer.getBytes(17, 17 + header.getBodyLength());
        ProtocolSerializerEnum serializerEnum = ProtocolSerializerEnum.getEnumByKey(header.getSerializer());
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum enumByKey = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if(enumByKey == null){
            throw new IOException("Unknown serializer");
        }
        switch(enumByKey){
            case REQUEST:
                RpcRequest request = serializer.deserialize(bytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case OTHERS:
            case HEART_BEAT:
            default:
                throw new IOException("暂不支持该类型");
        }



    }
}
