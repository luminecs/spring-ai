package org.springframework.ai.reader.pdf.layout;

class Character {

	private char characterValue;

	private int index;

	private boolean isCharacterPartOfPreviousWord;

	private boolean isFirstCharacterOfAWord;

	private boolean isCharacterAtTheBeginningOfNewLine;

	private boolean isCharacterCloseToPreviousWord;

	Character(char characterValue, int index, boolean isCharacterPartOfPreviousWord, boolean isFirstCharacterOfAWord,
			boolean isCharacterAtTheBeginningOfNewLine, boolean isCharacterPartOfASentence) {
		this.characterValue = characterValue;
		this.index = index;
		this.isCharacterPartOfPreviousWord = isCharacterPartOfPreviousWord;
		this.isFirstCharacterOfAWord = isFirstCharacterOfAWord;
		this.isCharacterAtTheBeginningOfNewLine = isCharacterAtTheBeginningOfNewLine;
		this.isCharacterCloseToPreviousWord = isCharacterPartOfASentence;
		if (ForkPDFLayoutTextStripper.DEBUG) {
			System.out.println(this.toString());
		}
	}

	public char getCharacterValue() {
		return this.characterValue;
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isCharacterPartOfPreviousWord() {
		return this.isCharacterPartOfPreviousWord;
	}

	public boolean isFirstCharacterOfAWord() {
		return this.isFirstCharacterOfAWord;
	}

	public boolean isCharacterAtTheBeginningOfNewLine() {
		return this.isCharacterAtTheBeginningOfNewLine;
	}

	public boolean isCharacterCloseToPreviousWord() {
		return this.isCharacterCloseToPreviousWord;
	}

	public String toString() {
		String toString = "";
		toString += this.index;
		toString += " ";
		toString += this.characterValue;
		toString += " isCharacterPartOfPreviousWord=" + this.isCharacterPartOfPreviousWord;
		toString += " isFirstCharacterOfAWord=" + this.isFirstCharacterOfAWord;
		toString += " isCharacterAtTheBeginningOfNewLine=" + this.isCharacterAtTheBeginningOfNewLine;
		toString += " isCharacterPartOfASentence=" + this.isCharacterCloseToPreviousWord;
		toString += " isCharacterCloseToPreviousWord=" + this.isCharacterCloseToPreviousWord;
		return toString;
	}

}
