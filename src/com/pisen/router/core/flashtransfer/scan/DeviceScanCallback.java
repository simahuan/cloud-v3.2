package com.pisen.router.core.flashtransfer.scan;

import com.pisen.router.core.flashtransfer.scan.protocol.ProtocolContext;

public interface DeviceScanCallback {

	void messageReceived(ProtocolContext cmd);

}
