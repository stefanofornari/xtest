var loaded = [];

Envjs.loadImage = function(node, src) {
    loaded.push(src);
    
    //
    // 'error' must be set by the bug free code executing this script
    //
    return !error;
};

//
//    WARNING! onerror must be before src otherwise it won't work; probably it
//    is a bug in Envjs.
//
window.location='src/test/resources/html/loadimageininnerhtml.html';
document.getElementsByTagName("body")[0].innerHTML = 
    "<img onerror='this.onerror=null;this.src=\"default.png\"' src='loadthis.jpg'>";
