class StorageStub {

    map = new Map();

    get length() {
        return this.map.size;
    };

    key(index) {
        let value = Array.from(this.map.keys())[index];
        return (value === undefined) ? null : value;
    }

    getItem(key) {
        let value = this.map.get(key);
        return (value === undefined) ? null : value;
    }

    setItem(key, value) {
        this.map.set(key, value);
    }

    removeItem(key) {
        this.map.delete(key);
    }

    clear() {
        this.map.clear();
    }
}