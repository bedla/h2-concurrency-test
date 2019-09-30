package cz.bedla.h2.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("C")
public class ActivityC extends Activity {
    @Column(name = "c_value")
    private String value;

    public ActivityC() {
    }

    public ActivityC(String value, long timeline) {
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
