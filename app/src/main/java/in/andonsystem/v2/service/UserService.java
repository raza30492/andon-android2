package in.andonsystem.v2.service;

import java.util.List;

import in.andonsystem.App;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.entity.UserDao;

/**
 * Created by razamd on 3/31/2017.
 */

public class UserService {

    private final UserDao userDao;

    public UserService(App app){
        userDao = app.getDaoSession().getUserDao();
    }

    public User findOne(Long id){
        return userDao.load(id);
    }

    public void saveOrUpdate(List<User> users){
        userDao.insertOrReplaceInTx(users);
    }

    public void deleteAll(){
        userDao.deleteAll();
    }
}
