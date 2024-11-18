/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
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
package com.timestored.misc;

import java.util.Iterator;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * Provide line wrapping for display of text in message boxes etc.
 */
public final class TextWrapper {

    enum Strategy implements WrapStrategy {
        HARD {

            @Override
            public String wrap(final Iterable<String> words, final int width) {
                return Joiner.on('\n')
                             .join(Splitter
                                    .fixedLength(width)
                                    .split(
                                        Joiner.on(' ').join(words)));
            }
        },
        SOFT {
            @Override
            public String wrap(final Iterable<String> words, final int width) {
                final StringBuilder sb = new StringBuilder();
                int lineLength = 0;
                final Iterator<String> iterator = words.iterator();
                if (iterator.hasNext()) {
                    sb.append(iterator.next());
                    lineLength=sb.length();
                    while (iterator.hasNext()) {
                        final String word = iterator.next();
                        if(word.length()+1+lineLength>width) {
                            sb.append('\n');
                            lineLength=0;
                        } else {
                            lineLength++;
                            sb.append(' ');
                        }
                        sb.append(word);
                        lineLength+=word.length();
                    }
                }
                return sb.toString();
            }
        }
    }

    interface WrapStrategy {
        String wrap(Iterable<String> words, int width);
    }

    public static TextWrapper forWidth(final int i) {
        return new TextWrapper(Strategy.SOFT, CharMatcher.whitespace(), i);
    }

    private final WrapStrategy  strategy;

    private final CharMatcher   delimiter;

    private final int           width;

    TextWrapper(final WrapStrategy strategy,
                final CharMatcher delimiter, final int width) {
        this.strategy = strategy;
        this.delimiter = delimiter;
        this.width = width;
    }

    public TextWrapper hard(){
        return new TextWrapper(Strategy.HARD, this.delimiter, this.width);
    }
    public TextWrapper respectExistingBreaks() {
        return new TextWrapper(
            this.strategy, CharMatcher.anyOf(" \t"), this.width);
    }

    public String wrap(final String text) {
        return this.strategy.wrap(
            Splitter.on(this.delimiter).split(text), this.width);
    }

}
