package com.stupm.core.handler;

import com.stupm.core.constant.ProtocolConstants;
import com.stupm.core.model.RpcRequest;
import com.stupm.core.model.RpcResponse;
import com.stupm.core.protocol.ProtocolMessage;
import com.stupm.core.protocol.ProtocolMessageTypeEnum;
import com.stupm.core.protocol.ProtocolSerializerEnum;
import com.stupm.core.serializer.Serializer;
import com.stupm.core.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageCodec;

import java.io.IOException;
import java.util.List;

@ChannelHandler.Sharable
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, ProtocolMessage<?>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage<?> message, List<Object> list) throws Exception {
        ProtocolMessage msg = (ProtocolMessage) message;
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeByte(msg.getHeader().getMagic());
        buffer.writeByte(msg.getHeader().getVersion());
        buffer.writeByte(msg.getHeader().getSerializer());
        buffer.writeByte(msg.getHeader().getType());
        buffer.writeByte(msg.getHeader().getStatus());
        buffer.writeLong(msg.getHeader().getRequestId());

        ProtocolSerializerEnum enumByKey = ProtocolSerializerEnum.getEnumByKey(msg.getHeader().getSerializer());
        if(enumByKey == null) {
            throw new RuntimeException("序列化协议不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(enumByKey.getValue());
        byte[] serialize = serializer.serialize(msg.getBody());
        buffer.writeInt(serialize.length);
        buffer.writeBytes(serialize);
        list.add(buffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        if(in.readableBytes() < 17)
            return;
        byte magic = in.readByte();
        if(magic != ProtocolConstants.PROTOCOL_MAGIC)
            throw new IOException("Invalid magic number");

        byte version = in.readByte();
        byte serializerId = in.readByte();
        byte type = in.readByte();
        byte status = in.readByte();
        long RequestId = in.readLong();
        int bodyLength = in.readInt();

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(version);
        header.setSerializer(serializerId);
        header.setType(type);
        header.setStatus(status);
        header.setRequestId(RequestId);
        header.setBodyLength(bodyLength);

        byte[] data = new byte[bodyLength];
        in.readBytes(data);

        ProtocolSerializerEnum serializerEnum = ProtocolSerializerEnum.getEnumByKey(header.getSerializer());
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum enumByKey = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if(enumByKey == null){
            throw new IOException("Unknown serializer");
        }
        switch(enumByKey){
            case REQUEST:
                RpcRequest request = serializer.deserialize(data, RpcRequest.class);
                list.add(new ProtocolMessage<>(header, request));
                in.markReaderIndex();
            case RESPONSE:
                RpcResponse response = serializer.deserialize(data, RpcResponse.class);
                list.add(new ProtocolMessage<>(header, response));
                in.markReaderIndex();
            case OTHERS:
            case HEART_BEAT:
            default:
                throw new IOException("暂不支持该类型");
        }
    }
}
