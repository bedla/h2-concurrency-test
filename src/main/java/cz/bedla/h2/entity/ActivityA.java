package cz.bedla.h2.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")
public class ActivityA extends Activity {
    @Column(name = "a_value")
    private String value;

    public ActivityA() {
    }

    public ActivityA(String value, long timeline) {
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
