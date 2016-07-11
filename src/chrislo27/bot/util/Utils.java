package chrislo27.bot.util;

import java.util.Random;

public class Utils {

	public static final Random random = new Random();
	public static final String[] alphabet;

	static {
		alphabet = new String[26];

		for (int start = 'A', i = start; i <= 'Z'; i++) {
			alphabet[i - start] = i + "";
		}
	}

	public static String getContent(String[] args, int start) {
		String content = "";
		for (int i = start; i < args.length; i++) {
			content += args[i];
			if (i != args.length - 1) {
				content += " ";
			}
		}

		return content;
	}

	public static String stripExtension(String fileName) {
		if (fileName.lastIndexOf('.') > 0) {
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}

		return fileName;
	}

	public static float clamp(float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public static int clamp(int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public static float lerp(float x, float y, float alpha) {
		return (float) lerp((double) x, y, alpha);
	}

	public static double lerp(double x, double y, double alpha) {
		return x + alpha * (y - x);
	}

}
