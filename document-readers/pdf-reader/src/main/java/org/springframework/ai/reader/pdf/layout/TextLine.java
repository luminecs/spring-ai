package org.springframework.ai.reader.pdf.layout;

import java.util.Arrays;

class TextLine {

	private static final char SPACE_CHARACTER = ' ';

	private final int lineLength;

	private final char[] line;

	private int lastIndex;

	TextLine(int lineLength) {
		if (lineLength < 0) {
			throw new IllegalArgumentException("Line length cannot be negative");
		}
		this.lineLength = lineLength / ForkPDFLayoutTextStripper.OUTPUT_SPACE_CHARACTER_WIDTH_IN_PT;
		this.line = new char[this.lineLength];
		Arrays.fill(this.line, SPACE_CHARACTER);
	}

	public void writeCharacterAtIndex(final Character character) {
		character.setIndex(this.computeIndexForCharacter(character));
		int index = character.getIndex();
		char characterValue = character.getCharacterValue();
		if (this.indexIsInBounds(index) && this.line[index] == SPACE_CHARACTER) {
			this.line[index] = characterValue;
		}
	}

	public int getLineLength() {
		return this.lineLength;
	}

	public String getLine() {
		return new String(this.line);
	}

	private int computeIndexForCharacter(final Character character) {
		int index = character.getIndex();
		boolean isCharacterPartOfPreviousWord = character.isCharacterPartOfPreviousWord();
		boolean isCharacterAtTheBeginningOfNewLine = character.isCharacterAtTheBeginningOfNewLine();
		boolean isCharacterCloseToPreviousWord = character.isCharacterCloseToPreviousWord();

		if (!this.indexIsInBounds(index)) {
			return -1;
		}
		else {
			if (isCharacterPartOfPreviousWord && !isCharacterAtTheBeginningOfNewLine) {
				index = this.findMinimumIndexWithSpaceCharacterFromIndex(index);
			}
			else if (isCharacterCloseToPreviousWord) {
				if (this.line[index] != SPACE_CHARACTER) {
					index = index + 1;
				}
				else {
					index = this.findMinimumIndexWithSpaceCharacterFromIndex(index) + 1;
				}
			}
			index = this.getNextValidIndex(index, isCharacterPartOfPreviousWord);
			return index;
		}
	}

	private boolean isNotSpaceCharacterAtIndex(int index) {
		return this.line[index] != SPACE_CHARACTER;
	}

	private boolean isNewIndexGreaterThanLastIndex(int index) {
		return index > this.lastIndex;
	}

	private int getNextValidIndex(int index, boolean isCharacterPartOfPreviousWord) {
		int nextValidIndex = index;
		if (!this.isNewIndexGreaterThanLastIndex(index)) {
			nextValidIndex = this.lastIndex + 1;
		}
		if (!isCharacterPartOfPreviousWord && index > 0 && this.isNotSpaceCharacterAtIndex(index - 1)) {
			nextValidIndex = nextValidIndex + 1;
		}
		this.lastIndex = nextValidIndex;
		return nextValidIndex;
	}

	private int findMinimumIndexWithSpaceCharacterFromIndex(int index) {
		int newIndex = index;
		while (newIndex >= 0 && this.line[newIndex] == SPACE_CHARACTER) {
			newIndex = newIndex - 1;
		}
		return newIndex + 1;
	}

	private boolean indexIsInBounds(int index) {
		return index >= 0 && index < this.lineLength;
	}

}
