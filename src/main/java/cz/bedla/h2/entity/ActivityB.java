package cz.bedla.h2.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")
public class ActivityB extends Activity {
    @Column(name = "b_value")
    private String value;

    public ActivityB() {
    }

    public ActivityB(String value, long timeline) {
        super(timeline);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
