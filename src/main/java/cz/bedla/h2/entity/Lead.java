package cz.bedla.h2.entity;

import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@SequenceGenerator(name = "pk_identity_lead", sequenceName = "seq_lead_id", initialValue = 1, allocationSize = 20)
public class Lead extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(generator = "pk_identity_lead", strategy = GenerationType.SEQUENCE)
    @Nullable
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "lead_id")
    @OrderBy("timeline")
    private List<Activity> activities;
    @Version
    private int version;

    @Override
    @Nullable
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(@Nullable Long id) {
        this.id = id;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
