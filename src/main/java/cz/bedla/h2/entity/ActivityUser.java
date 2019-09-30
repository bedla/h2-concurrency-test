package cz.bedla.h2.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("ASSIGN_RULE_USER")
public class ActivityUser extends Activity {
    @ManyToOne
    @JoinColumn(name = "assign_rule_user_id")
    private User user;

    public ActivityUser() {
    }

    public ActivityUser(User user, long timeline) {
        super(timeline);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
