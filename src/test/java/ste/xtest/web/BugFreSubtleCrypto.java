/*
 * xTest
 * Copyright (C) 2025 Stefano Fornari
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
package ste.xtest.web;

import java.io.File;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.BDDAssertions.then;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import ste.xtest.json.api.JSONAssertions;

/**
 * The web toolkit included in JavaFX does not support crypto.subtle. Aim of
 * this functionality is to provide a very basic and fake crypto.subtle API for
 * specification and testing purposes. Note that only what needed for a secret
 * based encryption/description is currently implemented. More functionality
 * will be added as needed.
 */
public class BugFreSubtleCrypto extends BugFreeWeb {

    @Before
    @Override
    public void before() throws Exception {
        FileUtils.copyDirectory(new File("src/main/resources/js/"), new File(localFileServer.root.toFile(), "js"));
        FileUtils.copyDirectory(new File("src/test/resources/html"), localFileServer.root.toFile());

        loadPage("hello.html");
    }

    @Test
    public void subtlecrypto_is_available() throws Exception {
        then(exec("window.crypto")).isNotEqualTo("undefined").isNotNull();
        then(exec("window.crypto.subtle")).isNotEqualTo("undefined").isNotNull();
    }

    @Test
    public void digest() throws Exception {
        checkDigest("SHA-256", "something", "5348412d3235362d313635323934353837000000000000000000000000000000");
        checkDigest("SHA-256", "something else", "5348412d3235362d313433373833373232350000000000000000000000000000");
        checkDigest("SHA-256", "", "5348412d3235362d300000000000000000000000000000000000000000000000");
        checkDigest("SHA-1", "something", "5348412d312d3136353239343538370000000000");
        checkDigest("SHA-384", "something", "5348412d3338342d31363532393435383700000000000000000000000000000000000000000000000000000000000000");
        checkDigest("SHA-512", "something", "5348412d3531322d3136353239343538370000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        checkDigest("SHA-520", "something", "Error: algorithm SHA-520 not supported for digest");
    }

    @Test
    public void import_key() throws Exception {
        //
        // Unknown format
        //
        exec("crypto.subtle.importKey('unknown', new TextEncoder().encode('1234'), {}, false, []);");
        then(errors).hasSize(1).element(0).hasToString("netscape.javascript.JSException: Error: key format UNKNOWN not supported for importKey");

        //
        // Fake password (raw/AES-GCM)
        //
        JSONObject key = (JSONObject) exec("""
            const pin = crypto.subtle.digest('SHA-256', new TextEncoder().encode('1234'));
            crypto.subtle.importKey('raw', pin, { name: 'AES-GCM' }, false, ['encrypt', 'decrypt']);
        """);
        JSONAssertions.then(key).isNotNull()
                .containsEntry("type", "secret")
                .containsEntry("extractable", false)
                .containsEntry("data", "5348412d3235362d323631353430323635390000000000000000000000000000"); // Specific to xtest implementation
        JSONAssertions.then(key.getJSONObject("algorithm")).isEqualTo(new JSONObject("{\"name\":\"AES-GCM\"}"));
        JSONAssertions.then(key.getJSONArray("usages")).containsExactly("encrypt", "decrypt");

        //
        // Fake private key import (pkcs8/RSA-PSS)
        //
        key = (JSONObject) exec("""
            crypto.subtle.importKey(
                "pkcs8",
                new TextEncoder().encode('1234'),
                {
                  name: "RSA-PSS",
                  hash: "SHA-256",
                },
                true,
                ["sign"],
            );
        """);
        JSONAssertions.then(key).isNotNull()
                .containsEntry("type", "private")
                .containsEntry("extractable", true)
                .containsEntry("data", "31323334");
        JSONAssertions.then(key.getJSONObject("algorithm")).isEqualTo(new JSONObject("{\"name\":\"RSA-PSS\"}"));
        JSONAssertions.then(key.getJSONArray("usages")).containsExactly("sign");

        //
        // Fake JWT key import (jwt/ECDSA)
        //
        key = (JSONObject) exec("""
            crypto.subtle.importKey(
                "jwt",
                {
                  "kty": "RSA",
                  "use": "sig",
                  "kid": "my-key-id",
                  "n": "ABCD",
                  "e": "EFGH"
                },
                {
                  name: "ECDSA",
                  namedCurve: "P-384",
                },
                true,
                ["sign"],
              );
        """);
        JSONAssertions.then(key).isNotNull()
            .containsEntry("type", "private")
            .containsEntry("extractable", true);
        JSONAssertions.then(key.getJSONObject("data")).isEqualTo(
            new JSONObject("{\"e\":\"EFGH\", \"kid\":\"my-key-id\", \"kty\":\"RSA\", \"n\":\"ABCD\", \"use\":\"sig\"}")
        );
        JSONAssertions.then(key.getJSONObject("algorithm")).isEqualTo(new JSONObject("{\"name\":\"ECDSA\"}"));
        JSONAssertions.then(key.getJSONArray("usages")).containsExactly("sign");

        //
        // Corner cases
        //
        // Null or empty key data
        final String[] VALUES = new String[]{
            "undefined", "null", "[]", "new ArrayBuffer()"
        };

        for (final String VALUE : VALUES) {
            then(
                    exec(String.format("""
                    try {
                        crypto.subtle.importKey('raw', %s, { name: 'AES-GCM' }, false, ['encrypt', 'decrypt']);
                    } catch (error) {
                        ret = error.toString();
                    }
                """, VALUE))
            ).isEqualTo("Error: key data can not be null or empty");
        }
    }

    @Test
    public void encrypt() throws Exception {
        //
        // Valid algorithm
        //
        exec("""
            const encoder = new TextEncoder();
            const decoder = new TextDecoder();
            const iv = new Uint8Array([01, 02, 03, 04, 05, 06, 07, 08, 09, 10, 11, 12]);

            const key = crypto.subtle.importKey(
                'raw',
                encoder.encode('1234'),
                { name: 'AES-GCM' },
                false,
                ['encrypt', 'decrypt']
            );
        """);

        for (final String A : new String[]{"AES-GCM", "AES-GCM", "RSA-OAEP"}) {
            then(exec(String.format("""
                crypto.subtle.hex(crypto.subtle.encrypt(
                    { name: '%s', iv: iv },
                    key,
                    encoder.encode('hello world')
            ));
            """, A))).isEqualTo("313233340102030405060708090a0b0c3a68656c6c6f20776f726c64");
        }

        //
        // Invalid algorithm
        //
        then(
            exec("""
            try {
                ret = crypto.subtle.encrypt(
                    { name: 'invalid', iv: iv },
                    key,
                    encoder.encode('hello world')
                );
            } catch (error) {
                ret = error.toString();
            }
        """)
        ).isEqualTo("Error: algorithm invalid not supported");

        //
        // Corner cases
        //
        // key data null or empty
        String[] VALUES = new String[]{
            "undefined", "null", "{}", "{data: null}", "{data: []}"
        };

        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.encrypt(
                        { name: 'AES-GCM', iv: iv },
                        %s,
                        encoder.encode('hello world')
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: key can not be null or empty");
        }

        // algorithm null or empty or invalid
        VALUES = new String[]{
            "undefined", null, "{}", "{noname:\"something\"}"
        };
        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.encrypt(
                        %s,
                        key,
                        encoder.encode('hello world')
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: algorithm null, empty or invalid");
        }

        // iv null or empty
        VALUES = new String[]{
            "", ", iv: undefined", ", iv: null", ", iv: []", ", iv: new ArrayBuffer()"
        };
        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.encrypt(
                        { name: 'AES-GCM' %s },
                        key,
                        encoder.encode('hello world')
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: algorithm does not contain a valid iv");
        }
    }

    @Test
    public void decrypt() throws Exception {
        //
        // Valid algorithm - positive cases
        //
        exec("""
            const encoder = new TextEncoder();
            const iv = new Uint8Array([65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76]);
            const encrypted = crypto.subtle.unhex('313233344142434445464748494a4b4c3a68656c6c6f20776f726c64');

            const key = crypto.subtle.importKey(
                'raw',
                encoder.encode('1234'),
                { name: 'AES-GCM' },
                false,
                ['encrypt', 'decrypt']
            );
        """);

        for (final String A : new String[]{"AES-GCM", "AES-GCM", "RSA-OAEP"}) {
            then(exec(String.format("""
                new TextDecoder().decode(crypto.subtle.decrypt(
                    { name: '%s', iv: iv },
                    key,
                    encrypted
                ));
            """, A))).isEqualTo("hello world");
        }

        //
        // Negative cases: return same as input
        //
        then(exec(String.format("""
            new TextDecoder().decode(crypto.subtle.decrypt(
                { name: 'AES-GCM', iv: iv },
                key,
                new Uint8Array([32, 33, 34, 35, 36])
            ));
        """))).isEqualTo(" !\"#$");

        then(exec(String.format("""
            new TextDecoder().decode(crypto.subtle.decrypt(
                { name: 'AES-GCM', iv: iv },
                { "type":"secret","algorithm":{ "name":"AES-GCM" },"data":"34333231" },
                encrypted
            ));
        """))).isNotEqualTo("1234:hello world");

        then(exec(String.format("""
            new TextDecoder().decode(crypto.subtle.decrypt(
                { name: 'AES-GCM', iv: new Uint8Array([65, 90]) },
                { "type":"secret","algorithm":{ "name":"AES-GCM" },"data":"34333231" },
                encrypted
            ));
        """))).isNotEqualTo("1234:hello world:AZ");

        //
        // Invalid algorithm
        //
        then(
            exec("""
            try {
                ret = crypto.subtle.decrypt(
                    { name: 'invalid', iv: iv },
                    key,
                    encrypted
                );
            } catch (error) {
                ret = error.toString();
            }
        """)
        ).isEqualTo("Error: algorithm invalid not supported");

        //
        // Corner cases
        //
        // key data null or empty
        String[] VALUES = new String[]{
            "undefined", "null", "{}", "{data: null}", "{data: []}"
        };

        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.decrypt(
                        { name: 'AES-GCM', iv: iv },
                        %s,
                        encrypted
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: key can not be null or empty");
        }

        // algorithm null or empty or invalid
        VALUES = new String[]{
            "undefined", null, "{}", "{noname:\"something\"}"
        };
        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.decrypt(
                        %s,
                        key,
                        encrypted
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: algorithm null, empty or invalid");
        }

        // iv null or empty
        VALUES = new String[]{
            "", ", iv: undefined", ", iv: null", ", iv: []", ", iv: new ArrayBuffer()"
        };
        for (final String VALUE : VALUES) {
            then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.decrypt(
                        { name: 'AES-GCM' %s },
                        key,
                        encrypted
                    );
                } catch (error) {
                    ret = error.toString();
                }
            """, VALUE))
            ).isEqualTo("Error: algorithm does not contain a valid iv");
        }
    }

    @Test
    public void not_implemented_interface_throws_exception() throws Exception {
        final String[][] METHODS = {
            new String[]{"sign", "'RSA-PSS', {}, new ArrayBuffer()"},
            new String[]{"verify", "{}, {}, new ArrayBuffer(), new ArrayBuffer()"},
            new String[]{"generateKey", "{}, true, []"},
            new String[]{"deriveKey", "{}, {}, {}, true, []"},
            new String[]{"deriveBits", "{}, {}, 0"},
            new String[]{"exportKey", "'raw', {}"},
            new String[]{"wrapKey", "'raw', {}, {}, {}"},
            new String[]{"unwrapKey", "'raw', {}, {}, {}, {}, true, []"}
        };

        for (String[] METHOD : METHODS) {
            then(exec(String.format("""
                try {
                    crypto.subtle.%s(%s);
                } catch(error) {
                    ret = error.toString();
                }
            """, METHOD[0], METHOD[1])))
                    .isEqualTo(String.format("Error: %s() not yet implemented", METHOD[0]));
        }
    }

    // --------------------------------------------------------- private methods
    void checkDigest(final String algorithm, final String text, final String expected) {
        then(
                exec(String.format("""
                try {
                    ret = crypto.subtle.hex(
                        crypto.subtle.digest('%s', new TextEncoder().encode('%s'))
                    );
                } catch(error) {
                    ret = error.toString();
                };
            """, algorithm, text))
        ).isEqualTo(expected);
    }

}
