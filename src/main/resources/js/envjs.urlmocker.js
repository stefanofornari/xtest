Envjs.map = new java.util.HashMap();
Envjs.selector = new Packages.ste.xtest.net.URLMockSelector(Envjs.map);

Envjs.buildURL = function (url) {
    return Envjs.selector.select(url);
}