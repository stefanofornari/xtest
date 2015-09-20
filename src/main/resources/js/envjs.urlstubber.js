Envjs.map = new java.util.HashMap();
Envjs.selector = new Packages.ste.xtest.net.URLStubSelector(Envjs.map);

Envjs.buildURL = function (url) {
    var selected = Envjs.selector.select(url);
    
    Envjs.debug("building url for %s\nmap: %s\nselected url: %s", url, Envjs.map.toString(), selected);
    
    return selected;
}