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

/**
 * This is a fake implementation of the SubtleCrypt Web API. It provides a basic
 * implementation of the API that can be used for testing purposes only.
 * This class DOES NOT PROVIDE any meaningful cryptographic alghoritm, but just
 * fake implementations that performs simple manipulation of the input data.
 * In particular:
 * <ul>
 *  <li>
 *  the entire interface is NOT for easiest testing and implementation
 *  (although it can normally be used with await
 *  <li>
 *  digest() always returns a zero-padded array of bytes representing the string
 *  $alghorithm-$crc32$
 *  where $alghoritm is the provided algorithm and crc32 is the crc32 of the
 *  input data
 *  <li>
 *  importKey() returns a CryptoKey with just basic information and type
 *  {@constant private} if algorithm is {@constant PKCS8} or {@constant JWT},
 *  {@constant secret} otherwise
 *  <li>
 *  encrypt() just returns an hex representation of the input data prepended
 *  key and iv plus a separator, ie.: $key$iv ':' $data
 * </ul>
 */
class FakeSubtleCrypto {

  ENCODER = new TextEncoder();
  SEPARATOR = "3a";

  decrypt(algorithm, key, data) {
    const ALLOWED_ALGORITHMS = " AES-CBC AES-CTR AES-CTR AES-GCM ";

    //
    // Argument validation
    //
    this.checkCryptoArguments(algorithm, key);

    const alg = algorithm.name.toUpperCase();

    if (ALLOWED_ALGORITHMS.indexOf(alg) < 0) {
        throw new Error(`Operation is not supported (algorithm ${algorithm.name} not supported)`);
    }

    //
    // if provided data is shorter than the key (which is hexed) something is
    // wrong and no decryption is performed
    //
    const prefix = key.data + this.hex(algorithm.iv) + this.SEPARATOR;
    if (data.length <= (prefix/2)) {
        throw Error("The operation failed for an operation-specific reason (decryption error)");
    }

    const encrypted = this.hex(data);

    //
    // if provided data does not start with key+iv+SEPARATOR, something is
    // wrong and no decryption is performed
    //
    if (encrypted.indexOf(prefix) !== 0) {
        throw Error("The operation failed for an operation-specific reason (decryption error)");
    }

    return this.unhex(encrypted.substring(prefix.length));
  }

  encrypt(algorithm, key, data) {
    const ALLOWED_ALGORITHMS = " AES-CBC AES-CTR AES-CTR AES-GCM ";

    //
    // Argument validation
    //
    this.checkCryptoArguments(algorithm, key);

    const alg = algorithm.name.toUpperCase();

    if (ALLOWED_ALGORITHMS.indexOf(alg) < 0) {
        throw new Error(`Operation is not supported (algorithm ${algorithm.name} not supported)`);
    }

    //
    // prepend key data and algorithm (note that key.data is alread "hexed"
    //
    return this.unhex(
        key.data + this.hex(algorithm.iv) + this.SEPARATOR + this.hex(data)
    );
  }

  sign(algorithm, key, data) {
    throw new Error(`sign() not yet implemented`);
  }

  verify(algorithm, key, signature, data) {
    throw new Error(`verify() not yet implemented`);
  }

  digest(algorithm, data) {
    const ALLOWED_ALGORITHMS = " SHA-1 SHA-256 SHA-384 SHA-512 ";
    const alg = algorithm.toUpperCase();

    if (ALLOWED_ALGORITHMS.indexOf(` ${alg} `) < 0) {
      throw new Error(`algorithm ${alg} not supported for digest`);
    }

    const digestString = alg + "-" + this.crc32(data);

    const howManyBits = parseInt(alg.substring(alg.indexOf("-")+1));
    const size = (howManyBits === 1) ? 20 : Math.ceil(howManyBits/8);

    const ret = new ArrayBuffer(size);
    const view = new Uint8Array(ret);
    view.set(this.ENCODER.encode(digestString));

    return ret;
  }

  generateKey(algorithm, extractable, keyUsages) {
    throw new Error(`generateKey() not yet implemented`);
  }

  deriveKey(algorithm, baseKey, derivedKeyAlgorithm, extractable, keyUsages) {
    throw new Error(`deriveKey() not yet implemented`);
  }

  deriveBits(algorithm, baseKey, length) {
    throw new Error(`deriveBits() not yet implemented`);
  }

  importKey(format, keyData, algorithm, extractable, keyUsages) {

    if ((keyData === null) || (keyData === undefined)
        || (keyData.length === 0) || (keyData.byteLength === 0)) {
        throw new Error("key data can not be null or empty");
    }

    const ALLOWED_FORMATS = " RAW PKCS8 SPKI JWT ";
    const frmt = format.toUpperCase();
    if (ALLOWED_FORMATS.indexOf(` ${frmt} `) < 0) {
      throw new Error(`key format ${frmt} not supported for importKey`);
    }

    if ((frmt === "PKCS8") || (frmt === "JWT")) {
        return {
            type: "private",
            extractable: extractable,
            algorithm: algorithm,
            usages: keyUsages,
            data: (frmt === "PKCS8") ? this.hex(keyData) : keyData
        };
    }

    return {
        type: "secret",
        extractable: extractable,
        algorithm: algorithm,
        usages: keyUsages,
        data: this.hex(keyData)
    };
  }

  exportKey(format, key) {
    throw new Error(`exportKey() not yet implemented`);
  }

  wrapKey(format, key, wrappingKey, wrapAlgorithm) {
    throw new Error(`wrapKey() not yet implemented`);

    //
    // Ideally the implementation should be straighfoward having exportKey:
    //
    // First export the key in the specified format
    //   const exportedKey = this.exportKey(format, key);
    //
    // Then encrypt the exported key using the wrapping key
    //   return this.encrypt(wrapAlgorithm, wrappingKey, exportedKey);
  }

  unwrapKey(format, wrappedKey, unwrappingKey, unwrapAlgorithm, unwrappedKeyAlgorithm, extractable, keyUsages) {
    throw new Error(`unwrapKey() not yet implemented`);

    //
    // Ideally the implementation should be straighfoward:
    //
    // First decrypt the wrapped key using the unwrapping key
    //   const exportedKey = this.decrypt(unwrapAlgorithm, unwrappingKey, wrappedKey);
    //
    // Then import the exported key in the specified format
    //   return this.importKey(format, exportedKey, unwrappedKeyAlgorithm, extractable, keyUsages);
  }

  // ----------------------------------------------------------- utility methods

  checkCryptoArguments(algorithm, key) {
    if (!key || !key.data || !key.data.length) {
      throw new Error("The operation failed for an operation-specific reason (key can not be null or empty)");
    }

    if (!algorithm || !algorithm.name || !algorithm.name.length) {
      throw new Error("The operation failed for an operation-specific reason (algorithm null, empty or invalid)");
    }

    //
    // iv is a ArrayBuffer...
    //
    if (!algorithm.iv || !algorithm.iv.byteLength) {
      throw new Error("The operation failed for an operation-specific reason (algorithm does not contain a valid iv)");
    }
  }

  /**
   * Converts a byte array into a string where each byte is represented by its
   * hexadecimal value (e.g. 0 -> 00, 10 -> 0a, 255 -> ff)
   *
   * @param {array} buffer
   * @returns {string}
   */
  hex(buffer) {
    //
    // NOTE: the current webkit in JavaFX does not implement Uint8Array.toHex()
    //
    return Array.from(new Uint8Array(buffer))
      .map(b => b.toString(16).padStart(2, '0'))
      .join('');
  }

  /**
   * Converts a string representation of a sequnce of bytes into a buffer
   * decoding a couple of chars into the corresponding decimal value
   * (e.g. '00' -> 0, '0a' -> 10, 'ff' -> 255)
   *
   * @param {string} hex sequence of hex values
   * @returns {buffer}
   */
  unhex(hex) {
    if (hex.length % 2 !== 0) {
      throw new Error('Hex string must have an even number of characters');
    }
    const result = new Uint8Array(hex.length / 2);
    for (let i = 0; i < hex.length; i += 2) {
      result[i / 2] = parseInt(hex.substr(i, 2), 16);
    }
    return result.buffer;
  }

  //
  // simple CRC function to easily compute an hash
  //
  // Example input        : [97, 98, 99, 100, 101] (Uint8Array)
  // Example output       : 2240272485 (Uint32)
  crc32(data) {
    var table = new Uint32Array(256);

    // Pre-generate crc32 polynomial lookup table
    // http://wiki.osdev.org/CRC32#Building_the_Lookup_Table
    // ... Actually use Alex's because it generates the correct bit order
    //     so no need for the reversal function
    for(var i=256; i--;) {
        var tmp = i;

        for(var k=8; k--;) {
            tmp = tmp & 1 ? 3988292384 ^ tmp >>> 1 : tmp >>> 1;
        }

        table[i] = tmp;
    }

    var crc = -1; // Begin with all bits set ( 0xffffffff )

    for(var i=0, l=data.length; i<l; i++) {
      crc = crc >>> 8 ^ table[ crc & 255 ^ data[i] ];
    }

    return (crc ^ -1) >>> 0; // Apply binary NOT
  }

}

// Create a polyfill for crypto.subtle if it doesn't exist
if (typeof crypto === 'undefined') {
  window.crypto = {};
}

if (typeof crypto.subtle === 'undefined') {
  crypto.subtle = new FakeSubtleCrypto();
}

// Export for use in Node.js or other environments
if (typeof module !== 'undefined' && module.exports) {
  module.exports = FakeSubtleCrypto;
}