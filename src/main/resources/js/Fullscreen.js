//
// stubbing document to support fullscreen
//
if (!document.__XTEST_FULLSCREEN_INITIALIZED__) {
    document.__XTEST_FULLSCREEN_INITIALIZED__ = true;
    document.originalGetElementById = document.getElementById;

    document.fullscreenEnabled = true;

    Object.defineProperty(document, 'fullscreenElement', {
        value: null,
        writable: true,
        configurable: true
    });

    document.exitFullscreen = function() {
        document.fullscreenElement = null;
    };

    document.getElementById = function(id) {
        const e = document.originalGetElementById(id);
        if (e) {
            e.requestFullscreen = function() {
                document.fullscreenElement = this;
            };
        }

        return e;
    };
}