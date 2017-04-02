package in.andonsystem.v2.service;

import android.util.Log;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;

import in.andonsystem.App;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.entity.BuyerDao;
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.entity.IssueDao;

/**
 * Created by razamd on 3/31/2017.
 */

public class IssueService {

    private final String TAG = IssueService.class.getSimpleName();

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
        Log.d(TAG, "findAll" );
        //return issueDao.queryDeep(" WHERE 1");
//        List<Issue> list =  issueDao.loadAll();
//        for (Issue i: list){
//            Log.i(TAG, "Buyer: " + i.getBuyer().getName());
//        }
//        return list;
        QueryBuilder<Issue> queryBuilder = issueDao.queryBuilder();
        queryBuilder.join(Buyer.class, BuyerDao.Properties.Id);
        return queryBuilder.list();
    }

    public List<Issue> findAllByTeam(String team){
        Log.d(TAG, "findAllByTeam: team = " + team);
        QueryBuilder<Issue> queryBuilder = issueDao.queryBuilder();
        queryBuilder.join(IssueDao.Properties.BuyerId,Buyer.class)
                .where(BuyerDao.Properties.Team.eq(team));
        //queryBuilder.
        List<Issue> list =  queryBuilder.list();
        for (Issue i: list){
            Log.i(TAG, "Buyer: " + i.getBuyer().getName());
            Log.i(TAG, "Team: " + i.getBuyer().getTeam());
        }
        return list;
    }

}
