/*
 *  Copyright (c) 2017 Works Applications Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.worksap.nlp.lucene.sudachi.ja.tokenattribute;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import com.worksap.nlp.lucene.sudachi.ja.util.Romanizer;
import com.worksap.nlp.sudachi.Morpheme;

public class ReadingAttributeImpl extends AttributeImpl implements ReadingAttribute, Cloneable {
    private Morpheme morpheme;

    public String getReading() {
    	if (morpheme == null) {
    		return null;
    	}
    	String reading = morpheme.readingForm();
        return reading == null || reading.isEmpty() ? morpheme.surface() : reading;
    }

    public String getPronunciation() {
    	if (morpheme == null) {
    		return null;
    	}
    	String reading = morpheme.readingForm();
        return reading == null || reading.isEmpty() ? morpheme.surface() : reading;
    }

    public void setMorpheme(Morpheme morpheme) {
        this.morpheme = morpheme;
    }

    @Override
    public void clear() {
        morpheme = null;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        ReadingAttribute t = (ReadingAttribute) target;
        t.setMorpheme(morpheme);
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
    	// Refer to org.apache.lucene.analysis.ja.tokenattributes.ReadingAttributeImpl 
        String reading = this.getReading();
        String readingEN = reading == null ? null : Romanizer.getRomanization(reading);
        String pronunciation = getPronunciation();
        String pronunciationEN = pronunciation == null ? null : Romanizer.getRomanization(pronunciation);
        reflector.reflect(ReadingAttribute.class, "reading", reading);
        reflector.reflect(ReadingAttribute.class, "reading (en)", readingEN);
        reflector.reflect(ReadingAttribute.class, "pronunciation", pronunciation);
        reflector.reflect(ReadingAttribute.class, "pronunciation (en)", pronunciationEN);
    }
}
