/*
 * xTest
 * Copyright (C) 2026 Stefano Fornari
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 */
package ste.xtest.llm;

import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

/**
 *
 */
public class BugFreeDummyTokenStream {

    @Test
    public void stream_from_a_string() {
        final String message = "Mirror, mirror on the wall: who’s the fairest of them all?";
        final DummyTokenStream s = new DummyTokenStream(message);

        final List<String> words = new ArrayList();

        s.onPartialResponse((w) -> words.add(w)).start();

        then(words).containsExactly(
            "Mirror", ",", " ", "mirror", " ", "on", " ", "the", " ", "wall",
            ":", " ", "who", "’", "s", " ", "the", " ", "fairest", " ",
            "of", " ", "them", " ", "all", "?"
        );

        final ChatResponse[] response = new ChatResponse[1];
        s.onCompleteResponse((r) -> response[0] = r).start();
        then(response[0].aiMessage().text()).isEqualTo(message);
    }
}
