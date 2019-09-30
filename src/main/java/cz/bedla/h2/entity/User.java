package cz.bedla.h2.entity;

import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Table(name = "app_user")
@SequenceGenerator(name = "pk_identity_app_user", sequenceName = "seq_app_user_id", initialValue = 1, allocationSize = 20)
public class User extends AbstractPersistable<Long> {
    @Id
    @GeneratedValue(generator = "pk_identity_app_user", strategy = GenerationType.SEQUENCE)
    @Nullable
    private Long id;
    private String login;

    public User() {
    }

    public User(String login) {
        this.login = login;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
