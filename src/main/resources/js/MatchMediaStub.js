class MatchMediaStub {
    constructor(initialState = {}) {
        this.state = {};
        this.MQLs = new Map();
        this.now = Date.now();

        if (initialState) {
            this.setMedia(initialState);
        }
    }

    // CSS Media Query Parser implementation
    #toDpi(value) {
        const matches = value.match(/^(\d+)dpi$/);
        return matches ? Number(matches[1]) : null;
    }

    #toDpcm(value) {
        const matches = value.match(/^(\d+)dpcm$/);
        return matches ? Number(matches[1]) * 2.54 : null;
    }

    #toPixels(value) {
        const matches = value.match(/^(\d+)px$/);
        return matches ? Number(matches[1]) : null;
    }

    #toMillimeters(value) {
        const matches = value.match(/^(\d+)mm$/);
        return matches ? Number(matches[1]) : null;
    }

    #toInches(value) {
        const matches = value.match(/^(\d+)in$/);
        return matches ? Number(matches[1]) : null;
    }

    #stripQuotes(str) {
        const quotes = ["'", '"'];
        return quotes.includes(str.charAt(0)) && quotes.includes(str.charAt(str.length - 1))
            ? str.slice(1, -1)
            : str;
    }

    #toDecimal(ratio) {
        const numbers = ratio.split('/').map(Number);
        return numbers[0] / numbers[1];
    }

    #parseFeature(expr) {
        const feature = {};
        const regExp = /\s*((min|max)-)?([a-z-]+)\s*:\s*((?:[a-z-]+|"[^"]+"|'[^']+'|\([^\)]+\)|\d+(?:\.\d+)?[a-z]*)+)\s*/;
        const match = regExp.exec(expr);

        if (!match) {
            feature.value = expr;
            return feature;
        }

        feature.modifier = match[2];
        feature.name = match[3];
        feature.value = this.#stripQuotes(match[4]);

        return feature;
    }

    parse(query) {
        const regExp = /(?:\s*([a-z-]+)\s*and\s*)?(?:\(\s*([^)]+)\s*\))/g;
        const results = [];
        let match;
        let type;

        query = query.toLowerCase();

        while ((match = regExp.exec(query))) {
            type = match[1] || 'all';

            const expressionMatch = match[2].trim();
            const feature = this.#parseFeature(expressionMatch);

            const expressions = [{
                feature: feature.name || feature.modifier,
                modifier: feature.modifier ? feature.modifier : undefined,
                value: feature.value
            }];

            results.push({
                type,
                expressions,
                inverse: query.indexOf('not ') === 0
            });
        }

        return results;
    }

    match(query, values) {
        const mediaQuery = this.parse(query);

        return mediaQuery.some(q => {
            const expressionsMatch = q.expressions.every(expression => {
                let value = values[expression.feature];
                let testValue = expression.value;

                // Convert units if necessary
                if (testValue.endsWith('dpi')) {
                    value = this.#toDpi(value);
                    testValue = this.#toDpi(testValue);
                } else if (testValue.endsWith('dpcm')) {
                    value = this.#toDpcm(value);
                    testValue = this.#toDpcm(testValue);
                } else if (testValue.endsWith('px')) {
                    value = this.#toPixels(value);
                    testValue = this.#toPixels(testValue);
                } else if (testValue.endsWith('mm')) {
                    value = this.#toMillimeters(value);
                    testValue = this.#toMillimeters(testValue);
                } else if (testValue.endsWith('in')) {
                    value = this.#toInches(value);
                    testValue = this.#toInches(testValue);
                } else if (testValue.includes('/')) {
                    value = this.#toDecimal(value);
                    testValue = this.#toDecimal(testValue);
                } else {
                    value = this.#stripQuotes(value);
                    testValue = this.#stripQuotes(testValue);
                }

                switch (expression.modifier) {
                    case 'min': return Number(value) >= Number(testValue);
                    case 'max': return Number(value) <= Number(testValue);
                    default: return value === testValue;
                }
            });

            return q.inverse ? !expressionsMatch : expressionsMatch;
        });
    }

    #getFeaturesFromQuery(query) {
        const parsedQuery = this.parse(query);
        const features = new Set();
        parsedQuery.forEach((subQuery) => {
            subQuery.expressions.forEach((expression) => {
                features.add(expression.feature);
            });
        });
        return features;
    }

    #createEventLegacy() {
        const now = this.now;
        return class EventLegacy {
            constructor(type) {
                this.type = type;
                this.timeStamp = Date.now() - now;

                this.bubbles = false;
                this.cancelBubble = false;
                this.cancelable = false;
                this.composed = false;
                this.target = null;
                this.currentTarget = null;
                this.defaultPrevented = false;
                this.eventPhase = 0;
                this.isTrusted = false;
                this.returnValue = true;
                this.srcElement = null;

                this.NONE = 0;
                this.CAPTURING_PHASE = 1;
                this.AT_TARGET = 2;
                this.BUBBLING_PHASE = 3;
            }

            initEvent() {}
            composedPath() { return []; }
            preventDefault() {}
            stopImmediatePropagation() {}
            stopPropagation() {}
        }
    }

    matchMedia(query) {
        const EventCompat = typeof Event === "undefined" ? this.#createEventLegacy() : Event;
        let queryTyped = query;
        let previousMatched;
        const self = this;

        try {
            previousMatched = this.match(queryTyped, this.state);
        } catch (e) {
            queryTyped = "not all";
            previousMatched = false;
        }

        const callbacks = new Set();
        const onces = new WeakSet();

        const clear = () => {
            for (const callback of callbacks) {
                onces.delete(callback);
            }
            callbacks.clear();
        };

        const removeListener = (callback) => {
            callbacks.delete(callback);
            onces.delete(callback);
        };

        const mql = {
            get matches() {
                return self.match(queryTyped, self.state);
            },
            media: query,
            onchange: null,
            addEventListener: (event, callback, options) => {
                if (event === "change" && callback) {
                    const isAlreadyListed = callbacks.has(callback);
                    callbacks.add(callback);

                    const hasOnce = typeof options === "object" && options?.once;

                    if (!hasOnce) {
                        onces.delete(callback);
                        return;
                    }

                    if (isAlreadyListed && !onces.has(callback)) {
                        return;
                    }

                    onces.add(callback);
                }
            },
            removeEventListener: (event, callback) => {
                if (event === "change") removeListener(callback);
            },
            dispatchEvent: (event) => {
                if (!event) {
                    throw new TypeError(
                        `Failed to execute 'dispatchEvent' on 'EventTarget': 1 argument required, but only 0 present.`
                    );
                }
                if (!(event instanceof EventCompat)) {
                    throw new TypeError(
                        `Failed to execute 'dispatchEvent' on 'EventTarget': parameter 1 is not of type 'Event'.`
                    );
                }
                if (event.type !== "change") {
                    return true;
                }
                mql.onchange?.(event);
                callbacks.forEach((callback) => {
                    callback(event);
                    if (onces.has(callback)) {
                        removeListener(callback);
                    }
                });
                return true;
            },
            addListener: (callback) => {
                if (!callback) return;
                callbacks.add(callback);
            },
            removeListener: (callback) => {
                if (!callback) return;
                removeListener(callback);
            },
        };

        this.MQLs.set(mql, {
            previousMatched,
            clear,
            features: this.#getFeaturesFromQuery(queryTyped),
        });

        return mql;
    }

    setMedia(media) {
        let changedFeatures = new Array();  // we should use Set but in some
                                            // webkit versions it does not work
        Object.keys(media).forEach((feature) => {
            if (changedFeatures.indexOf(feature) < 0) {
                changedFeatures.push(feature);
            }
            this.state[feature] = media[feature];
        });

        for (const [MQL, cache] of this.MQLs) {
            let found = false;
            for (const feature of cache.features) {
                if (changedFeatures.indexOf(feature) >= 0) {
                    found = true;
                    break;
                }
            }

            const matches = this.match(MQL.media, this.state);
            if (matches === cache.previousMatched) {
                continue;
            }
            cache.previousMatched = matches;

            const event = new (typeof Event === "undefined" ? this.#createEventLegacy() : Event)("change");
            event.matches = matches;
            event.media = MQL.media;

            MQL.dispatchEvent(event);
        }
    }

    cleanup() {
        for (const { clear } of this.MQLs.values()) {
            clear();
        }
        this.MQLs.clear();
        this.state = {};
    }
}
