package org.apache.lucene.analysis.miscellaneous;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.tests.analysis.MockTokenizer;
import org.junit.Test;

public class TestConcatenateWordsFilterTest extends BaseTokenStreamTestCase {
	@Test
	public void testConcatenatedWords() throws IOException {
		String input = "this is a very simple test and it should pass";
		TokenStream ts = new MockTokenizer(MockTokenizer.WHITESPACE, false);
		((Tokenizer) ts).setReader(new StringReader(input));
		ts = new ConcatenateWordsFilter(ts);
		assertTokenStreamContents(ts, new String[] { "thisisaverysimpletestanditshouldpass" });
	}

	@Test
	public void testEmptyTerm() throws IOException {
		Analyzer a = new Analyzer() {
			@Override
			protected TokenStreamComponents createComponents(String fieldName) {
				Tokenizer tokenizer = new KeywordTokenizer();
				return new TokenStreamComponents(tokenizer, new ConcatenateWordsFilter(tokenizer));
			}
		};
		checkOneTerm(a, "", "");
	}

	@Test
	public void testOffSets() throws IOException {
		String input = "another simple test";
		TokenStream ts = new MockTokenizer(MockTokenizer.WHITESPACE, false);
		((Tokenizer) ts).setReader(new StringReader(input));
		ts = new ConcatenateWordsFilter(ts);
		assertTokenStreamContents(ts, new String[] { "anothersimpletest" }, new int[] { 0 }, new int[] { 19 });
	}

	/** blast some random strings through the analyzer */
	@Test
	public void testRandomString() throws Exception {
		Analyzer a = new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String fieldName) {
				Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
				return new TokenStreamComponents(tokenizer, new ConcatenateWordsFilter(tokenizer));
			}
		};
		checkRandomData(random(), a, 1000 * RANDOM_MULTIPLIER);
	}

}
