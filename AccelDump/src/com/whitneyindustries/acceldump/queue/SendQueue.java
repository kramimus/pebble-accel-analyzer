package com.whitneyindustries.acceldump.queue;

import com.whitneyindustries.acceldump.model.AccelData;

public interface SendQueue {
    void addNewReading(AccelData reading);
    int sendUnsent();
}
