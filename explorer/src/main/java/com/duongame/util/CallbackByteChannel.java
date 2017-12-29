package com.duongame.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Created by namjungsoo on 2017-12-28.
 */

public class CallbackByteChannel implements WritableByteChannel {
    public interface ProgressCallback {
        void callback(CallbackByteChannel wbc, int progress);
    }

    private CallbackByteChannel wbc;
    private ProgressCallback callback;
    private long sizeWrite;
    private long size;

    public CallbackByteChannel(CallbackByteChannel wbc, ProgressCallback callback, long size) {
        this.wbc = wbc;
        this.callback = callback;
        this.size = size;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int n;
        int progress;
        if ((n = wbc.write(src)) > 0) {
            sizeWrite += n;
            //progress = (int)(size > 0 ? (double) sizeWrite / (double) size * 100.0) : -1;
            //callback.callback(this, progress);
        }
        return n;
    }

    @Override
    public boolean isOpen() {
        return wbc.isOpen();
    }

    @Override
    public void close() throws IOException {
        wbc.close();
    }
}
