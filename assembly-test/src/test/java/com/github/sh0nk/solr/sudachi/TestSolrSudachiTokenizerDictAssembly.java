package com.github.sh0nk.solr.sudachi;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.Test;
import static org.apache.lucene.tests.analysis.BaseTokenStreamTestCase.assertTokenStreamContents;
import static org.apache.lucene.tests.analysis.BaseTokenStreamTestCase.newAttributeFactory;

public class TestSolrSudachiTokenizerDictAssembly extends SolrTestCaseJ4 {

    private Tokenizer createTokenizer(Map<String, String> args) throws IOException {
        // Load from bundled dictionary
        Map<String, String> map = new HashMap<>(args);
        SolrSudachiTokenizerFactory factory = new SolrSudachiTokenizerFactory(map);
        factory.inform(new SolrResourceLoader(Paths.get(URI.create("."))));
        return factory.create(newAttributeFactory());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        File testHome = createTempDir("core-home").toFile();
        System.setProperty("solr.solr.home", testHome.getAbsolutePath());
    }

    @Override
    public void tearDown() throws Exception {
        System.clearProperty("solr.solr.home");
        super.tearDown();
    }

    @Test
    public void testDefault() throws Exception {
        initCore();

        Tokenizer tokenizer = createTokenizer(Collections.emptyMap());
        tokenizer.setReader(new StringReader("吾輩は猫である。"));
        assertTokenStreamContents(tokenizer,
                new String[] {"我が輩", "は", "猫", "だ", "有る"}
        );
    }

    @Test
    public void testOnlySettings() throws Exception {
        initCore();

        Map<String, String> args = new HashMap<>();
        args.put("settingsPath", "solr_sudachi.json");
        Tokenizer tokenizer = createTokenizer(args);
        tokenizer.setReader(new StringReader("吾輩は猫である。"));
        assertTokenStreamContents(tokenizer,
                new String[] {"我が輩", "は", "猫", "だ", "有る"}
        );
    }

    @Test
    public void testOnlyDictDir() throws Exception {
        initCore();

        Map<String, String> args = new HashMap<>();
        String dir = createTempDir().toString();
        args.put("systemDictDir", dir);
        Tokenizer tokenizer = createTokenizer(args);
        tokenizer.setReader(new StringReader("吾輩は猫である。"));
        assertTokenStreamContents(tokenizer,
                new String[] {"我が輩", "は", "猫", "だ", "有る"}
        );
    }

}