package org.springframework.ai.util;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class LoggingMarkers {

	public static final Marker SENSITIVE_DATA_MARKER = MarkerFactory.getMarker("SENSITIVE");

	public static final Marker RESTRICTED_DATA_MARKER = MarkerFactory.getMarker("RESTRICTED");

	public static final Marker REGULATED_DATA_MARKER = MarkerFactory.getMarker("REGULATED");

	public static final Marker PUBLIC_DATA_MARKER = MarkerFactory.getMarker("PUBLIC");

	private LoggingMarkers() {

	}

}
