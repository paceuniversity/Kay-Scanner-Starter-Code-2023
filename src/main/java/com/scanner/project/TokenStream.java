package com.scanner.project;
// TokenStream.java

// Implementation of the Scanner for KAY

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TokenStream {

	// Instance variables 
	private boolean isEof = false; // is end of file
	private char nextChar = ' '; // next character in input stream
	private BufferedReader input;

	// This function was added to make the demo file work
	public boolean isEoFile() {
		return isEof;
	}

	// Constructor
	public TokenStream(String fileName) {
		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + fileName);
			isEof = true;
		}
	}

	public Token nextToken() { // Main function of the scanner
		Token t = new Token();
		t.setType("Other"); // For now it is Other
		t.setValue("");

		// First check for whitespaces and bypass them
		skipWhiteSpace();

		// Then check for a comment, and bypass it
		while (nextChar == '/') {
			// Changed if to while to avoid the 2nd line being printed when
			// there are two comment lines in a row.
			nextChar = readChar();
			if (nextChar == '/') { // If / is followed by another /
				// skip rest of line - it's a comment.
				do {
					nextChar = readChar();
				} while (!isEndOfLine(nextChar) && !isEof);

				skipWhiteSpace();
			} else {
				// A slash followed by anything else must be an operator.
				t.setValue("/");
				t.setType("Operator");
				return t;
			}
		}

		// Then check for an operator; this part of the code should recover 2-character
		// operators as well as 1-character ones.
		if (isOperator(nextChar)) {
			t.setType("Operator");
			t.setValue(t.getValue() + nextChar);
			
			switch (nextChar) {
				case '<':
					// <=
					nextChar = readChar();
					if (nextChar == '='){
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					}
					return t;
				case '>':
					// >=
					nextChar = readChar();
					if (nextChar == '='){
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					}
					return t;
				case ':':
					// == is :=
					nextChar = readChar();
					if (nextChar == '='){
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					} else {
						t.setType("Other");
					}
					return t;
				case '=': 
					// ==
					nextChar = readChar();
					if (nextChar == '='){
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					} else {
						// Single = is 'Other'
						t.setType("Other");
					}
					return t;
				case '!':
					// !=
					nextChar = readChar();
					if (nextChar == '='){
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					}
					return t;
				case '|':
					// Look for ||
					nextChar = readChar();
					if (nextChar == '|') {
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					} else {
						// Single | is 'Other'
						t.setType("Other");
					}
					return t;
				case '&':
					// Look or &&
					nextChar = readChar();
					if (nextChar == '&') {
						t.setValue(t.getValue() + nextChar);
						nextChar = readChar();
					} else {
						// Single & is 'Other'
						t.setType("Other");
					}
					return t;
				default: // all other operators (+, -, *)
					nextChar = readChar();
					return t;
			}
		}

		// Then check for a separator
		if (isSeparator(nextChar)) {
			t.setType("Separator");
			t.setValue(t.getValue() + nextChar);
			nextChar = readChar();
			return t;
		}

		// Then check for an identifier, keyword, or literal.
		if (isLetter(nextChar)) {
			// Set to an identifier
			t.setType("Identifier");
			while ((isLetter(nextChar) || isDigit(nextChar))) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			// now see if this is a keyword
			if (isKeyword(t.getValue())) {
				t.setType("Keyword");
			} else if (t.getValue().equals("True") || t.getValue().equals("False")) {
				t.setType("Literal");
			}

			if (isEndOfToken(nextChar)) {
				return t;
			}
		}

		if (isDigit(nextChar)) { // check for integer literals
			t.setType("Literal");
			while (isDigit(nextChar)) {
				t.setValue(t.getValue() + nextChar);
				nextChar = readChar();
			}
			if (isEndOfToken(nextChar)) {
				return t;
			}
		}

		// If none of the above, it is an unknown/Other character
		t.setType("Other");
		
		if (isEof) {
			return t;
		}

		// Consume characters until we hit a delimiter
		while (!isEndOfToken(nextChar)) {
			t.setValue(t.getValue() + nextChar);
			nextChar = readChar();
		}
		
		skipWhiteSpace();

		return t;
	}

	private char readChar() {
		int i = 0;
		if (isEof)
			return (char) 0;
		System.out.flush();
		try {
			i = input.read();
		} catch (IOException e) {
			System.exit(-1);
		}
		if (i == -1) {
			isEof = true;
			return (char) 0;
		}
		return (char) i;
	}

	private boolean isKeyword(String s) {
		return (s.equals("bool") || s.equals("else") || s.equals("if") || s.equals("integer") || s.equals("main") || s.equals("while"));
	}

	private boolean isWhiteSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfLine(char c) {
		return (c == '\r' || c == '\n' || c == '\f');
	}

	private boolean isEndOfToken(char c) { 
		return (isWhiteSpace(nextChar) || isOperator(nextChar) || isSeparator(nextChar) || isEof);
	}

	private void skipWhiteSpace() {
		while (!isEof && isWhiteSpace(nextChar)) {
			nextChar = readChar();
		}
	}

	private boolean isSeparator(char c) {
		return (c == '(' || c == ')' || c == '{' || c == '}' || c == ';' || c == ',');
	}

	private boolean isOperator(char c) {
		// Checks for characters that start operators
		return (c == '+' || c == '-' || c == '*' || c == '/' || c == '<' || c == '>' || c == '=' || c == '!' || c == '&' || c == '|' || c == ':');
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	public boolean isEndofFile() {
		return isEof;
	}
}
