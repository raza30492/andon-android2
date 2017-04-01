package in.andonsystem.v2.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by razamd on 3/31/2017.
 */

@Entity
public class User {
    @Id
    private Long id;

    @NotNull
    @Property(nameInDb = "user_name")
    private String name;

    private String email;

    private String mobile;

    private String role;

    @Property(nameInDb = "user_type")
    private String userType;

    private String level;

    @Generated(hash = 274549443)
    public User(Long id, @NotNull String name, String email, String mobile,
            String role, String userType, String level) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.role = role;
        this.userType = userType;
        this.level = level;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public User(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserType() {
        return this.userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
