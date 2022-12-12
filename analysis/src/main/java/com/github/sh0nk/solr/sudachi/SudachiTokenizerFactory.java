package com.github.sh0nk.solr.sudachi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.ResourceLoader;
import org.apache.lucene.util.ResourceLoaderAware;

import com.worksap.nlp.lucene.sudachi.ja.SudachiTokenizer;
import com.worksap.nlp.sudachi.Settings;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

public class SudachiTokenizerFactory extends TokenizerFactory implements ResourceLoaderAware {

	private static final String MODE = "mode";
    private static final String MODE_A = "a";
    private static final String MODE_B = "b";
    private static final String MODE_C = "c";
    private static final String DISCARD_PUNCTUATION = "discardPunctuation";
    private static final String SYSTEM_DICT_DIR = "systemDictDir";
    private static final String SETTINGS_PATH = "settingsPath";

	private final SplitMode mode;
    private final boolean discardPunctuation;
    private final String settingsPath;
    private final String systemDictDir;

    private String fixedSystemDictDir = null;
    private String settingsContent = null;

	public SudachiTokenizerFactory(Map<String, String> args) {
		super(args);
		this.mode = getMode(get(args, MODE));
		this.discardPunctuation = getBoolean(args, DISCARD_PUNCTUATION, true);
		this.settingsPath = get(args, SETTINGS_PATH, "solr_sudachi.json");
		this.systemDictDir = get(args, SYSTEM_DICT_DIR);
	}

	private SplitMode getMode(String input) {
		if (input != null) {
            if (MODE_A.equalsIgnoreCase(input)) {
                return SplitMode.A;
            } else if (MODE_B.equalsIgnoreCase(input)) {
                return SplitMode.B;
            } else if (MODE_C.equalsIgnoreCase(input)) {
                return SplitMode.C;
            }
        }
        return SudachiTokenizer.DEFAULT_MODE;
	}

	@Override
	public void inform(ResourceLoader resourceLoader) throws IOException {
		ResourceLoaderHelper resourceLoaderHelper = new ResourceLoaderHelper(resourceLoader);
        if (systemDictDir != null) {
            fixedSystemDictDir = resourceLoaderHelper.getResourcePath(systemDictDir).toString();
        } else {
            fixedSystemDictDir = resourceLoaderHelper.getConfigDir();
        }

        ensureSettings(resourceLoaderHelper);
        ensureSystemDict();

	}
    private void ensureSettings(ResourceLoaderHelper resourceLoaderHelper) throws IOException {
        if (fileExists(settingsPath)) {
            settingsContent = getSettingsFileContent(Paths.get(settingsPath));
        } else if (fileExists(resourceLoaderHelper.getConfigDir(), settingsPath)) {
            settingsContent = getSettingsFileContent(Paths.get(resourceLoaderHelper.getConfigDir(), settingsPath));
        } else {
            settingsContent = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(settingsPath),
            		StandardCharsets.UTF_8);
        }
    }

    private void ensureSystemDict() throws IOException {
        String systemDictPath = Settings.parseSettings(fixedSystemDictDir, settingsContent).getString("systemDict");

        // 1. abs path on json
        // 2. file with base(fixedSystemDictDir) dir
        // (1.2. is how JapaneseDictionary works)
        if (fileExists(systemDictPath) || fileExists(fixedSystemDictDir, systemDictPath)) {
            return;
        }

        // 3. try to extract with the name to base dir, once extraction is done, from the next time 2. should work
        // if it's not matched, thrown
        new DictExtractor(systemDictPath).extractTo(fixedSystemDictDir);
    }
    private static boolean fileExists(String file, String... more) {
        return StringUtils.isNotEmpty(file) && Files.exists(Paths.get(file, more));
    }

    private static String getSettingsFileContent(Path fixedSettingsPath) {
        if (fixedSettingsPath == null) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(fixedSettingsPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read Sudachi settings file from " + fixedSettingsPath);
        }
    }
	@Override
	public Tokenizer create(AttributeFactory arg0) {
		try {
            return new SudachiTokenizer(discardPunctuation, mode, fixedSystemDictDir, settingsContent);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to create SolrSudachiTokenizer", e);
        }

	}

	
}
