package in.andonsystem.v2.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by razamd on 3/31/2017.
 */

@Entity
public class Issue {

    @Id
    private Long id;

    @ToOne
    private Buyer buyer;

    private String problem;

    private String description;

    @ToOne
    private User raisedBy;

    @ToOne
    private User ackBy;

    @ToOne
    private User fixBy;

    private Date raisedAt;

    private Date ackAt;

    private Date fixAt;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 724440415)
    private transient IssueDao myDao;

    @Generated(hash = 1767985888)
    public Issue(Long id, String problem, String description, Date raisedAt,
            Date ackAt, Date fixAt) {
        this.id = id;
        this.problem = problem;
        this.description = description;
        this.raisedAt = raisedAt;
        this.ackAt = ackAt;
        this.fixAt = fixAt;
    }

    @Generated(hash = 596101413)
    public Issue() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProblem() {
        return this.problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getRaisedAt() {
        return this.raisedAt;
    }

    public void setRaisedAt(Date raisedAt) {
        this.raisedAt = raisedAt;
    }

    public Date getAckAt() {
        return this.ackAt;
    }

    public void setAckAt(Date ackAt) {
        this.ackAt = ackAt;
    }

    public Date getFixAt() {
        return this.fixAt;
    }

    public void setFixAt(Date fixAt) {
        this.fixAt = fixAt;
    }

    @Generated(hash = 660281577)
    private transient boolean buyer__refreshed;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 306222603)
    public Buyer getBuyer() {
        if (buyer != null || !buyer__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BuyerDao targetDao = daoSession.getBuyerDao();
            targetDao.refresh(buyer);
            buyer__refreshed = true;
        }
        return buyer;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 313168657)
    public Buyer peakBuyer() {
        return buyer;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 458266812)
    public void setBuyer(Buyer buyer) {
        synchronized (this) {
            this.buyer = buyer;
            buyer__refreshed = true;
        }
    }

    @Generated(hash = 765375038)
    private transient boolean raisedBy__refreshed;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1036059164)
    public User getRaisedBy() {
        if (raisedBy != null || !raisedBy__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            targetDao.refresh(raisedBy);
            raisedBy__refreshed = true;
        }
        return raisedBy;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 490536577)
    public User peakRaisedBy() {
        return raisedBy;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1177325456)
    public void setRaisedBy(User raisedBy) {
        synchronized (this) {
            this.raisedBy = raisedBy;
            raisedBy__refreshed = true;
        }
    }

    @Generated(hash = 1170753193)
    private transient boolean ackBy__refreshed;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1831051859)
    public User getAckBy() {
        if (ackBy != null || !ackBy__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            targetDao.refresh(ackBy);
            ackBy__refreshed = true;
        }
        return ackBy;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 1434835337)
    public User peakAckBy() {
        return ackBy;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 771730307)
    public void setAckBy(User ackBy) {
        synchronized (this) {
            this.ackBy = ackBy;
            ackBy__refreshed = true;
        }
    }

    @Generated(hash = 818812211)
    private transient boolean fixBy__refreshed;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 946316743)
    public User getFixBy() {
        if (fixBy != null || !fixBy__refreshed) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserDao targetDao = daoSession.getUserDao();
            targetDao.refresh(fixBy);
            fixBy__refreshed = true;
        }
        return fixBy;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 984639832)
    public User peakFixBy() {
        return fixBy;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1672018782)
    public void setFixBy(User fixBy) {
        synchronized (this) {
            this.fixBy = fixBy;
            fixBy__refreshed = true;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 884668014)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getIssueDao() : null;
    }


}
