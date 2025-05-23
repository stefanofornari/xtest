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

var __XTEST__ = {
    matchMediaStub: null,
    lastResult: null,
    log: "",
    ready: false
};

window.matchMedia = (query) => {
    return __XTEST__.matchMediaStub.matchMedia(query);
};

//
// capturing console messages
//
const consoleLogging = (function(srdConsole){
    return {
        out: function(severity, arguments) {
            __XTEST__.log += `${severity} ${Array.from(arguments).join(" ")}\n`
        },
        log: function(...text){
            srdConsole.log(...text);
            this.out("L", text)
        },
        info: function (...text) {
            srdConsole.info(...text);
            this.out("I", text)
        },
        warn: function (...text) {
            srdConsole.warn(...text);
            this.out("W", text)
        },
        error: function (...text) {
            srdConsole.error(...text);
            this.out("E", text)
        },
        debug: function (...text) {
            srdConsole.debug(...text);
            this.out("D", text)
        }
    };
}(window.console));

window.console = consoleLogging;
Date = DateStub;

