package com.cyberwalkabout.common.util;

import java.io.Serializable;

public class Bounds implements Serializable
{
	private double latNorthEastern;
	private double lonNorthEastern;
	private double latSouthWestern;
	private double lonSouthWestern;

	public Bounds()
	{
	}

	public Bounds(double latNorthEastern, double lonNorthEastern, double latSouthWestern, double lonSouthWestern)
	{
		this.latNorthEastern = latNorthEastern;
		this.lonNorthEastern = lonNorthEastern;
		this.latSouthWestern = latSouthWestern;
		this.lonSouthWestern = lonSouthWestern;
	}

	public double getLatNorthEastern()
	{
		return latNorthEastern;
	}

	public void setLatNorthEastern(double latNorthEastern)
	{
		this.latNorthEastern = latNorthEastern;
	}

	public double getLonNorthEastern()
	{
		return lonNorthEastern;
	}

	public void setLonNorthEastern(double lonNorthEastern)
	{
		this.lonNorthEastern = lonNorthEastern;
	}

	public double getLatSouthWestern()
	{
		return latSouthWestern;
	}

	public void setLatSouthWestern(double latSouthWestern)
	{
		this.latSouthWestern = latSouthWestern;
	}

	public double getLonSouthWestern()
	{
		return lonSouthWestern;
	}

	public void setLonSouthWestern(double lonSouthWestern)
	{
		this.lonSouthWestern = lonSouthWestern;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latNorthEastern);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(latSouthWestern);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lonNorthEastern);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lonSouthWestern);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bounds other = (Bounds) obj;
		if (Double.doubleToLongBits(latNorthEastern) != Double.doubleToLongBits(other.latNorthEastern))
			return false;
		if (Double.doubleToLongBits(latSouthWestern) != Double.doubleToLongBits(other.latSouthWestern))
			return false;
		if (Double.doubleToLongBits(lonNorthEastern) != Double.doubleToLongBits(other.lonNorthEastern))
			return false;
		if (Double.doubleToLongBits(lonSouthWestern) != Double.doubleToLongBits(other.lonSouthWestern))
			return false;
		return true;
	}
}