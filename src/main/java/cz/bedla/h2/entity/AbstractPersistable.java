package cz.bedla.h2.entity;

import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Note: modified from Spring Data JPA source
 * <p>
 * Abstract base class for entities. Allows parameterization of id type, chooses auto-generation and implements
 * {@link #equals(Object)} and {@link #hashCode()} based on that id.
 *
 * @param <PK> the type of the identifier.
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Mark Paluch
 */
@MappedSuperclass
public abstract class AbstractPersistable<PK extends Serializable> implements Persistable<PK> {
    /**
     * Sets the id of the entity.
     *
     * @param id the id to set
     */
    protected abstract void setId(@Nullable PK id);

    /**
     * Must be {@link Transient} in order to ensure that no JPA provider complains because of a missing setter.
     *
     * @see Persistable#isNew()
     */
    @Transient // DATAJPA-622
    @Override
    public boolean isNew() {
        return null == getId();
    }

    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
            return false;
        }

        AbstractPersistable<?> that = (AbstractPersistable<?>) obj;

        return this.getId() != null && this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == getId() ? 0 : getId().hashCode() * 31;
        return hashCode;
    }
}
