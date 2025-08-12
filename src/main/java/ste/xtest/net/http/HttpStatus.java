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

package ste.xtest.net.http;

public enum HttpStatus {
    CONTINUE(100, "Continue"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NOT_AUTHORITATIVE(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET(205, "Reset Content"),
    PARTIAL(206, "Partial Content"),
    MULT_CHOICE(300, "Multiple Choices"),
    MOVED_PERM(301, "Moved Permanently"),
    MOVED_TEMP(302, "Temporary Redirect"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    BAD_METHOD(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTH(407, "Proxy Authentication Required"),
    CLIENT_TIMEOUT(408, "Request Time-Out"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECON_FAILED(412, "Precondition Failed"),
    ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQ_TOO_LONG(414, "Request-URI Too Large"),
    UNSUPPORTED_TYPE(415, "Unsupported Media Type"),
    INTERNAL_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    VERSION(505, "HTTP Version Not Supported");

    public final int code;
    public final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return code + " " + message;
    }

    public static HttpStatus fromCode(int code) {
        for (HttpStatus status : HttpStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("No HTTP status code " + code + " found");
    }
}