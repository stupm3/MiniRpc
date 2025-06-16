package com.stupm.core.server.tcp;

import com.stupm.core.constant.RpcConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;


    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecodeParser(bufferHandler);
    }

    private RecordParser initRecodeParser(Handler<Buffer> bufferHandler) {
        RecordParser parser = RecordParser.newFixed(RpcConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if(size == -1){
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    resultBuffer.appendBuffer(buffer);
                }else{
                    resultBuffer.appendBuffer(buffer);
                    bufferHandler.handle(resultBuffer);
                    parser.fixedSizeMode(RpcConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });
        return parser;
    }


    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
}
