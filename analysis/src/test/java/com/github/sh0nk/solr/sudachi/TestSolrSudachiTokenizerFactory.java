package com.github.sh0nk.solr.sudachi;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.tests.analysis.BaseTokenStreamTestCase;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.Test;

public class TestSolrSudachiTokenizerFactory extends BaseTokenStreamTestCase {

	private Tokenizer createTokenizer(Map<String, String> args) throws IOException, URISyntaxException {
		String dictDir = new File(getClass().getResource("/system_full.dic").toURI()).getParent();
		String settingsFile = getClass().getResource("/solr_sudachi.json").getPath();
		Map<String, String> map = new HashMap<>(args);
		map.put("systemDictDir", dictDir);
		map.put("settingsPath", settingsFile);
		SolrSudachiTokenizerFactory factory = new SolrSudachiTokenizerFactory(map);
		factory.inform(new SolrResourceLoader(Paths.get(".")));
		return factory.create(newAttributeFactory());
	}

	@Test
	public void testDefault() throws IOException, URISyntaxException {
		Tokenizer tokenizer = createTokenizer(new HashMap<>());
		tokenizer.setReader(new StringReader("私は猫である。"));
		System.out.println(tokenizer);
		assertTokenStreamContents(tokenizer, new String[] { "私", "は", "猫", "だ", "有る" });
	}

	@Test
	public void testPunctuation() throws IOException, URISyntaxException {
		@SuppressWarnings("serial")
		Tokenizer tokenizer = createTokenizer(new HashMap<>() {
			{
				put("discardPunctuation", "false");
			}
		});
		tokenizer.setReader(new StringReader("私は猫である。"));
		assertTokenStreamContents(tokenizer, new String[] { "私", "は", "猫", "だ", "有る", "。" });
	}

	@Test
	public void testExtended() throws IOException, URISyntaxException {
		@SuppressWarnings("serial")
		Tokenizer tokenizer = createTokenizer(new HashMap<>() {
			{
				put("mode", "EXTENDED");
			}
		});
		tokenizer.setReader(new StringReader("アブラカタブラ"));
		assertTokenStreamContents(tokenizer, new String[] { "アブラカタブラ", "ア", "ブ", "ラ", "カ", "タ", "ブ", "ラ" },
				new int[] { 1, 0, 1, 1, 1, 1, 1, 1 });
	}

}