
URLSearchParams = function(search) {
    if (!search || !search.trim()) {
        throw new Packages.java.lang.IllegalArgumentException("search can not be empty");
    }
    this.QS = Packages.ste.web.http.QueryString.parse(search);
};

URLSearchParams.prototype = {
    get: function(p) {
        //
        // I am doing like this to return a String instead of JavaNativeObject
        //
        var v = this.QS.get(p);
        if (v) {
            return "" + v;
        }
        
        return null;
    }
};