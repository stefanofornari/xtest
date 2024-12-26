/*
 * url processing in the spirit of python's urlparse module
 * see `pydoc urlparse` or
 * http://docs.python.org/library/urlparse.html
 *
 *  urlsplit: break apart a URL into components
 *  urlunsplit:  reconsistute a URL from componets
 *  urljoin: join an absolute and another URL
 *  urldefrag: remove the fragment from a URL
 *
 * Take a look at the tests in urlparse-test.html
 *
 * On URL Normalization:
 *
 * urlsplit only does minor normalization the components Only scheme
 * and hostname are lowercased urljoin does a bit more, normalizing
 * paths with "."  and "..".

 * urlnormalize adds additional normalization
 *
 *   * removes default port numbers
 *     http://abc.com:80/ -> http://abc.com/, etc
 *   * normalizes path
 *     http://abc.com -> http://abc.com/
 *     and other "." and ".." cleanups
 *   * if file, remove query and fragment
 *
 * It does not do:
 *   * normalizes escaped hex values
 *     http://abc.com/%7efoo -> http://abc.com/%7Efoo
 *   * normalize '+' <--> '%20'
 *
 * Differences with Python
 *
 * The javascript urlsplit returns a normal object with the following
 * properties: scheme, netloc, hostname, port, path, query, fragment.
 * All properties are read-write.
 *
 * In python, the resulting object is not a dict, but a specialized,
 * read-only, and has alternative tuple interface (e.g. obj[0] ==
 * obj.scheme).  It's not clear why such a simple function requires
 * a unique datastructure.
 *
 * urlunsplit in javascript takes an duck-typed object,
 *  { scheme: 'http', netloc: 'abc.com', ...}
 *  while in  * python it takes a list-like object.
 *  ['http', 'abc.com'... ]
 *
 * For all functions, the javascript version use
 * hostname+port if netloc is missing.  In python
 * hostname+port were always ignored.
 *
 * Similar functionality in different languages:
 *
 *   http://php.net/manual/en/function.parse-url.php
 *   returns assocative array but cannot handle relative URL
 *
 * TODO: test allowfragments more
 * TODO: test netloc missing, but hostname present
 */

var urlparser = {};

// Unlike to be useful standalone
//
// NORMALIZE PATH with "../" and "./"
//   http://en.wikipedia.org/wiki/URL_normalization
//   http://tools.ietf.org/html/rfc3986#section-5.2.3
//
urlparser.normalizepath = function(path)
{
    if (!path || path === '/') {
        return '/';
    }

    var parts = path.split('/');

    var newparts = [];
    // make sure path always starts with '/'
    if (parts[0]) {
        newparts.push('');
    }

    for (var i = 0; i < parts.length; ++i) {
        if (parts[i] === '..') {
            if (newparts.length > 1) {
                newparts.pop();
            } else {
                newparts.push(parts[i]);
            }
        } else if (parts[i] != '.') {
            newparts.push(parts[i]);
        }
    }

    path = newparts.join('/');
    if (!path) {
        path = '/';
    }
    return path;
};

//
// Does many of the normalizations that the stock
//  python urlsplit/urlunsplit/urljoin neglects
//
// Doesn't do hex-escape normalization on path or query
//   %7e -> %7E
// Nor, '+' <--> %20 translation
//
urlparser.urlnormalize = function(url)
{
    var parts = urlparser.urlsplit(url);
    switch (parts.scheme) {
    case 'http':
    case 'https':
        // remove default port
        if ((parts.scheme === 'http' && parts.port == 80) ||
            (parts.scheme === 'https' && parts.port == 443)) {
            parts.port = null;
            // hostname is already lower case
            parts.netloc = parts.hostname;
        }
        break;
    default:
        // if we don't have specific normalizations for this
        // scheme, return the original url unmolested
        return url;
    }

    // for [file|http|https].  Not sure about other schemes
    parts.path = urlparser.normalizepath(parts.path);

    return urlparser.urlunsplit(parts);
};

urlparser.urldefrag = function(url)
{
    var idx = url.indexOf('#');
    if (idx == -1) {
        return [ url, '' ];
    } else {
        return [ url.substr(0,idx), url.substr(idx+1) ];
    }
};

urlparser.urlsplit = function(url, default_scheme, allow_fragments)
{
    var leftover;

    if (typeof allow_fragments === 'undefined') {
        allow_fragments = true;
    }

    // scheme (optional), host, port
    var fullurl = /^([A-Za-z]+)?(:?\/\/)([0-9.\-A-Za-z]*)(?::(\d+))?(.*)$/;
    // path, query, fragment
    var parse_leftovers = /([^?#]*)?(?:\?([^#]*))?(?:#(.*))?$/;

    var o = {};

    var parts = url.match(fullurl);
    if (parts) {
        o.scheme = parts[1] || default_scheme || '';
        o.hostname = parts[3].toLowerCase() || '';
        o.port = parseInt(parts[4],10) || '';
        // Probably should grab the netloc from regexp
        //  and then parse again for hostname/port

        o.netloc = parts[3];
        if (parts[4]) {
            o.netloc += ':' + parts[4];
        }

        leftover = parts[5];
    } else {
        o.scheme = default_scheme || '';
        o.netloc = '';
        o.hostname = '';
        leftover = url;
    }
    o.scheme = o.scheme.toLowerCase();

    //
    // if schame is file, net.loc may contain relative path notation ('.' or '..')
    //
    if (o.scheme == 'file') {
        if (o.netloc.charAt(0) == '.') {
            leftover = o.netloc + leftover;
            o.netloc = '';
        }
    }

    parts = leftover.match(parse_leftovers);

    o.path =  parts[1] || '';
    o.query = parts[2] || '';

    if (allow_fragments) {
        o.fragment = parts[parts.length-1] || '';
    } else {
        o.fragment = '';
    }

    return o;
};

urlparser.urlunsplit = function(o) {
    var s = '';
    if (o.scheme) {
        s += o.scheme + '://';
    }

    if (o.netloc) {
        if (s == '') {
            s += '//';
        }
        s +=  o.netloc;
    } else if (o.hostname) {
        // extension.  Python only uses netloc
        if (s == '') {
            s += '//';
        }
        s += o.hostname;
        if (o.port) {
            s += ':' + o.port;
        }
    }

    if (o.path) {
        s += o.path;
    }

    if (o.query) {
        s += '?' + o.query;
    }
    if (o.fragment) {
        s += '#' + o.fragment;
    }
    return s;
};

urlparser.urljoin = function(base, url, allow_fragments)
{
    if (typeof allow_fragments === 'undefined') {
        allow_fragments = true;
    }

    var url_parts = urlparser.urlsplit(url);

    // if url parts has a scheme and path does not start with '.' (i.e. absolute)
    // then nothing to do;
    if (url_parts.scheme && !(url_parts.path.charAt(0) == '.')) {
        if (! allow_fragments) {
            return url;
        } else {
            return urlparser.urldefrag(url)[0];
        }
    }
    var base_parts = urlparser.urlsplit(base);

    // copy base, only if not present
    if (!base_parts.scheme) {
        base_parts.scheme = url_parts.scheme;
    }

    // copy netloc, only if not present and scame is not file
    if ((base_parts.scheme != 'file') && (!base_parts.netloc || !base_parts.hostname)) {
        base_parts.netloc = url_parts.netloc;
        base_parts.hostname = url_parts.hostname;
        base_parts.port = url_parts.port;
    }

    // paths
    if (url_parts.path.length > 0) {
        if (url_parts.path.charAt(0) == '/') {
            base_parts.path = url_parts.path;
        } else {
            // relative path.. get rid of "current filename" and
            //   replace.  Same as var parts =
            //   base_parts.path.split('/'); parts[parts.length-1] =
            //   url_parts.path; base_parts.path = parts.join('/');
            var idx = base_parts.path.lastIndexOf('/');
            if (idx == -1) {
                base_parts.path = url_parts.path;
            } else {
                base_parts.path = base_parts.path.substr(0,idx) + '/' +
                    url_parts.path;
            }
        }
    }

    // clean up path
    base_parts.path = urlparser.normalizepath(base_parts.path);

    // copy query string
    base_parts.query = url_parts.query;

    // copy fragments
    if (allow_fragments) {
        base_parts.fragment = url_parts.fragment;
    } else {
        base_parts.fragment = '';
    }

    return urlparser.urlunsplit(base_parts);
};

