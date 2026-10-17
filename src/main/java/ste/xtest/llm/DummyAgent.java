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

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import java.util.logging.Logger;

import ste.lazyjava.annotation.NonBlank;

@SystemMessage("this is the system prompt")
public interface DummyAgent {

    Logger LOG = Logger.getLogger(DummyAgent.class.getName());

    default TokenStream stream(@NonBlank final String userMessage) {
        LOG.info(() -> "streaming with " + userMessage);
        return _stream_(userMessage);
    }

    default String text(@NonBlank final String userMessage) {
        LOG.info(() -> "texting with " + userMessage);
        return _text_(userMessage);
    }

    TokenStream _stream_(@UserMessage String userMessage);
    String  _text_(@UserMessage String userMessage);

}
