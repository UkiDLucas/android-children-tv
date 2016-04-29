package com.cyberwalkabout.common.util;

/**
 * @author Andrii Kovalov
 */
public class MathUtils
{
	public static double normalize(double dataLow, double dataHigh, double normalizedLow, double normalizedHigh, double value)
	{
		return ((value - dataLow) / (dataHigh - dataLow)) * (normalizedHigh - normalizedLow) + normalizedLow;
	}

	public static double denormalize(double dataLow, double dataHigh, double normalizedLow, double normalizedHigh, double value)
	{
		return ((dataLow - dataHigh) * value - normalizedHigh * dataLow + dataHigh * normalizedLow) / (normalizedLow - normalizedHigh);
	}
}
