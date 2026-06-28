package basementhost.randomchad.util;

import java.security.SecureRandom;

public class CodeUtil {

	private static final SecureRandom RANDOM = new SecureRandom();

	private CodeUtil() {
	}

	public static String generateCode(int length, String characters) {
		StringBuilder codeBuilder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			int index = RANDOM.nextInt(characters.length());
			codeBuilder.append(characters.charAt(index));
		}

		return codeBuilder.toString();
	}
}