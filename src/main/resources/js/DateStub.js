class DateStub extends Date {
    static fixedDate = null;

    constructor(...args) {
        if (args.length === 0 && DateStub.fixedDate) {
            return DateStub.fixedDate;
        } else {
            super(...args);
        }
    }
}