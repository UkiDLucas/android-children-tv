package com.cyberwalkabout.common.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AsciiIdGenerator
{
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private int currentId;
	private int minLength;
	private SharedPreferences prefs;

	public AsciiIdGenerator(Context ctx)
	{
		this(ctx, 5);
	}

	public AsciiIdGenerator(Context ctx, int minLength)
	{
		this.prefs = ctx.getSharedPreferences("ascii_id_generator", Context.MODE_PRIVATE);
		this.currentId = prefs.getInt("last_id", 0);
		this.minLength = minLength;
	}

	public String nextId()
	{
		int id = currentId++;
		StringBuilder b = new StringBuilder();
		while (true)
		{
			b.append(ALPHABET.charAt(id % ALPHABET.length()));
			if ((id /= ALPHABET.length()) == 0 && b.length() >= minLength) break;
		}
		prefs.edit().putInt("last_id", currentId).commit();
		return b.toString();
	}
}