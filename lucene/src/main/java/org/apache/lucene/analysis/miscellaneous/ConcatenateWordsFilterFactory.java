package org.apache.lucene.analysis.miscellaneous;

import java.util.Map;

import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.analysis.TokenStream;

public class ConcatenateWordsFilterFactory extends TokenFilterFactory {

	public ConcatenateWordsFilterFactory(Map<String, String> args) {
		super(args);
		if (!args.isEmpty()) {
			throw new IllegalArgumentException("Unknown parameters: " + args);
		}
	}

	@Override
	public TokenStream create(TokenStream input) {
		return new ConcatenateWordsFilter(input);
	}

}
