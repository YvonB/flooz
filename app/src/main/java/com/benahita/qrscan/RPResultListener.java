package com.benahita.qrscan;

interface RPResultListener {
    void onPermissionGranted();

    void onPermissionDenied();
}
