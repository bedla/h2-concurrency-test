package cz.bedla.h2.entity;

import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Table(name = "lead_activity")
@Inheritance
@DiscriminatorColumn(name = "discriminator")
@SequenceGenerator(name = "pk_identity_activity", sequenceName = "seq_lead_activity_id", initialValue = 1, allocationSize = 20)
public class Activity extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(generator = "pk_identity_activity", strategy = GenerationType.SEQUENCE)
    @Nullable
    private Long id;

    private long timeline;

    public Activity() {
    }

    public Activity(long timeline) {
        this.timeline = timeline;
    }

    @Override
    @Nullable
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(@Nullable Long id) {
        this.id = id;
    }

    public long getTimeline() {
        return timeline;
    }

    public void setTimeline(long timeline) {
        this.timeline = timeline;
    }
}
