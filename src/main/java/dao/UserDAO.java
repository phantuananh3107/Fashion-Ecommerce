package dao;

import java.util.List;
import java.util.Map;

import model.UserObject;

public interface UserDAO {
	

    List<UserObject> getAllUsers(String keyword, String sortBy);
    UserObject getUserById(int user_id);

    boolean deactivateUser(int user_id);

    public UserObject getUserByUsernamePassword(String username, String password);
    public int getTotalUserCount();
    public Map<String, Integer> getUserGenderCounts();
    public Map<String, Integer> getUserStatusCounts();
    public Map<String, Integer> getNewUsersByMonth();
    public Map<String, Integer> getUserRoleCounts();
    public boolean insertUser(UserObject user);
    public boolean updateUser(UserObject user);
    public List<UserObject> getInactiveSoonUsers(int limit);
    void lockUserAccount(String email);
    String checkUserExistsDetail(String fullname, String email, String phone);
}
