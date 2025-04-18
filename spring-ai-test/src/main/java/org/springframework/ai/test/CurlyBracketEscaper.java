package org.springframework.ai.test;

public class CurlyBracketEscaper {

	public static String escapeCurlyBrackets(String input) {
		if (input == null) {
			return null;
		}
		return input.replace("{", "\\{").replace("}", "\\}");
	}

	public static String unescapeCurlyBrackets(String input) {
		if (input == null) {
			return null;
		}
		return input.replace("\\{", "{").replace("\\}", "}");
	}

}
