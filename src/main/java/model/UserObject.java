package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserObject {

	private int userId;
	private String password;
	private String gender;
	private String fullname;
	private String phoneNumber;
	private String email;
	private Date createDate;
	private Date modifiedDate;
	private int active;
	private String address;
	private int loginCount;
	private RoleObject role;
	private int flag;
}
