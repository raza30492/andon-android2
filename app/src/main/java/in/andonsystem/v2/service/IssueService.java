package in.andonsystem.v2.service;

import java.util.Date;
import java.util.List;

import in.andonsystem.App;
import in.andonsystem.v2.entity.BuyerDao;
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.entity.IssueDao;

/**
 * Created by razamd on 3/31/2017.
 */

public class IssueService {
    private final IssueDao issueDao;

    public IssueService(App app){
        issueDao = app.getDaoSession().getIssueDao();
    }

    public void saveOrUpdate(List<Issue> issues){
        issueDao.insertOrReplaceInTx(issues);
    }

    public void deleteAllOlder(){
        Long time = new Date().getTime();
        Date midnight = new Date(time - time % (24 * 60 * 60 * 1000));
        List<Issue> issues = issueDao.queryBuilder()
                .where(IssueDao.Properties.RaisedAt.lt(midnight))
                .list();
        issueDao.deleteInTx(issues);
    }

    public List<Issue> findAll(){
        return issueDao.loadAll();
    }

}
