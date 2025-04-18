package org.springframework.ai.model;

import java.util.ArrayList;
import java.util.List;

public final class EmbeddingUtils {

	private static final float[] EMPTY_FLOAT_ARRAY = new float[0];

	private EmbeddingUtils() {

	}

	public static List<Float> doubleToFloat(final List<Double> doubles) {
		return doubles.stream().map(f -> f.floatValue()).toList();
	}

	public static float[] toPrimitive(List<Float> floats) {
		return toPrimitive(floats.toArray(new Float[floats.size()]));
	}

	public static float[] toPrimitive(final Float[] array) {
		if (array == null) {
			return null;
		}
		if (array.length == 0) {
			return EMPTY_FLOAT_ARRAY;
		}
		final float[] result = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i].floatValue();
		}
		return result;
	}

	public static Float[] toFloatArray(final float[] array) {
		if (array == null) {
			return null;
		}
		if (array.length == 0) {
			return new Float[0];
		}
		final Float[] result = new Float[array.length];
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static List<Float> toList(float[] floats) {

		List<Float> output = new ArrayList<Float>();
		for (float value : floats) {
			output.add(value);
		}
		return output;
	}

}
