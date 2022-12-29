package org.apache.lucene.analysis.miscellaneous;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;

public final class ConcatenateWordsFilter extends TokenFilter {
	private CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
	private OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
	private PositionLengthAttribute positionLengthAttribute = addAttribute(PositionLengthAttribute.class);
	private StringBuilder stringBuilder = new StringBuilder();
	private State savedState = null;
	private boolean exhausted = false;
	private int lastEndOffset = 0;

	public ConcatenateWordsFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		while (!exhausted && input.incrementToken()) {
			char[] terms = charTermAttribute.buffer();
			int termLength = charTermAttribute.length();
			int positionLength = positionLengthAttribute.getPositionLength();
			lastEndOffset = offsetAttribute.endOffset();
			if (positionLength == 1) {
				stringBuilder.append(terms, 0, termLength);
			}
			if (savedState == null) {
				savedState = captureState();
			}
		}
		if (!exhausted) {
			concatenateTokens();
			exhausted = true;
			return true;
		}
		return false;

	}

	@Override
	public void reset() throws IOException {
		super.reset();
		stringBuilder.setLength(0);
		savedState = null;
		exhausted = false;
		lastEndOffset = 0;
	}

	private void concatenateTokens() {
		restoreState(savedState);
		char[] terms = charTermAttribute.buffer();
		int totalLength = stringBuilder.length();
		if (totalLength > charTermAttribute.length()) {
			terms = charTermAttribute.resizeBuffer(totalLength);
		}
		stringBuilder.getChars(0, totalLength, terms, 0);
		charTermAttribute.setLength(totalLength);
		offsetAttribute.setOffset(offsetAttribute.startOffset(), lastEndOffset);
		stringBuilder.setLength(0);
	}

}
