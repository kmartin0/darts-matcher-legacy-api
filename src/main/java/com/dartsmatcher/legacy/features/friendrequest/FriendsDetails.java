package com.dartsmatcher.legacy.features.friendrequest;

import com.dartsmatcher.legacy.features.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendsDetails {
	private ArrayList<User> friendsDetails;
}
