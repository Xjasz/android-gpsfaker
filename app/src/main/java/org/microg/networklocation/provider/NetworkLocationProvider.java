package org.microg.networklocation.provider;

import android.location.Location;
import android.os.IBinder;

import org.microg.networklocation.data.LocationCalculator;

public interface NetworkLocationProvider {
	String NETWORK_LOCATION_TYPE = "networkLocationType";

	void disable();

	void enable();

	IBinder getBinder();

	boolean isActive();

	void onLocationChanged(Location paramLocation) throws SecurityException;

	void setCalculator(LocationCalculator locationCalculator);
}
