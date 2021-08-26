package com.google.sample.cast.castconnect;

import android.content.Context;
import com.google.android.gms.cast.tv.CastReceiverOptions;
import com.google.android.gms.cast.tv.ReceiverOptionsProvider;

public class CastReceiverOptionsProvider implements ReceiverOptionsProvider {
    @Override
    public CastReceiverOptions getOptions(Context context) {
        return new CastReceiverOptions.Builder(context)
                .setStatusText("Cast Connect Codelab")
                .build();
    }
}