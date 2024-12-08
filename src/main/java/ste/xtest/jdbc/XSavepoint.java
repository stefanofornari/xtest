package ste.xtest.jdbc;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Acolyte savepoint.
 *
 * @author Cedric Chantepie
 */
public final class XSavepoint implements java.sql.Savepoint {
    // --- Properties ---

    /**
     * Id
     */
    private final int id;

    /**
     * Name
     */
    private final String name;

    // --- Constructor ---

    /**
     * No-name constructor.
     */
    public XSavepoint() {
        this.id = System.identityHashCode(this);
        this.name = null;
    } // end of <init>

    /**
     * Named savepoint constructor.
     *
     * @param name Savepoint name
     */
    public XSavepoint(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Invalid name");
        } // end of if

        this.id = System.identityHashCode(this);
        this.name = name;
    } // end of <init>

    // --- Properties ---

    /**
     * {@inheritDoc}
     */
    public int getSavepointId() {
        return this.id;
    } // end of getSavepointId

    /**
     * {@inheritDoc}
     */
    public String getSavepointName() {
        return this.name;
    } // end of getSavepointName

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return String.format("Savepoint(%s, %s)", this.id, this.name);
    } // end of toString

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(1, 3).
            append(this.id).append(this.name).
            toHashCode();

    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSavepoint)) {
            return false;
        } // end of if

        final XSavepoint other = (XSavepoint) o;

        return new EqualsBuilder().
            append(this.id, other.id).
            append(this.name, other.name).
            isEquals();

    } // end of equals
} // end of class Savepoint
