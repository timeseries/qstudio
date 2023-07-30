package com.timestored.qstudio.model;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * For one given {@link ServerModel} allows retrieving and setting permissions on that server.
 */
public class UserPermissionsModel {

	private static final String GROUP1_ID = "Super Users";
	private static final String GROUP2_ID = "Power Users";
	private static final String GROUP3_ID = "Users";
	private static final String GROUP4_ID = "Sales Users";
	
	private final ServerModel serverModel;

	private List<Group> allGroups = null;
	private List<User> allUsers = null;

	public UserPermissionsModel(ServerModel serverModel) {
		this.serverModel = serverModel;
		
		retrieveCurrentSettings();
	}
	
	public String getServerName() {
		return serverModel.getName();
	}
	
	
	public ServerModel getServerModel() {
		return serverModel;
	}

	/**
	 * Attempt to add a user and return true if successful.
	 */
	public boolean addUser(String userId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to delete a user  and return true if successful.
	 */
	public boolean deleteUser(User user) {
		throw new UnsupportedOperationException();
	}

	public void setUsersGroups(User user, List<Group> groups) {
		throw new UnsupportedOperationException();
	}

	public void updateGroup(Group group, String newName, GroupType newGroupType,
			List<String> newPermissionedEntities) {
		throw new UnsupportedOperationException();
	}

	public void setGroupsUsers(Group group, List<User> users) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to add a group and return true if successful.
	 */
	public boolean addGroup(String groupId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to delete a user and report true if new user added successfully.
	 */
	public boolean deleteGroup(Group group) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Attempt to delete a user and report true if new user added successfully.
	 */
	public boolean setUsersGroup(List<Group> groups) {
		return false;
	}
	
	public List<User> getUsers() {
		return allUsers;
	}
	
	public List<User> getUsers(Group group) {
		ArrayList<User> r = new ArrayList<User>();
		for(User u : allUsers) {
			if(u.groupIds.contains(group.id)) {
				r.add(u);
			}
		}
		return r;
	}
	
	public List<Group> getGroups() {
		return allGroups;
	}

	public List<Group> getGroups(User user) {
		ArrayList<Group> r = new ArrayList<Group>();
		for(Integer groupId : user.groupIds) {
			for(Group g : allGroups) {
				if(g.id == groupId) {
					r.add(g);
				}
			}
		}
		assert(r.size() == user.groupIds.size());
		return r;
	}
	
	private void retrieveCurrentSettings() {

		User superUser = new User(0, "superUser-Ian", ImmutableList.of(0));
		User powerUser = new User(1, "admin-John", ImmutableList.of(1));
		User basicUser = new User(2, "Ryan", ImmutableList.of(2));
		User salesUser1 = new User(3, "SalesSimon", ImmutableList.of(1, 3));
		User salesUser2 = new User(4, "SalesStewart", ImmutableList.of(3));
		
		allUsers = ImmutableList.of(superUser, powerUser, basicUser, salesUser1, salesUser2);

		Group g1 = new Group(0, GROUP1_ID, GroupType.SUPER_USER, null);
		Group g2 = new Group(1, GROUP2_ID, GroupType.POWER_USER, null);
		List<String> permittedFuncs = ImmutableList.of(".sys.load",".report.bondSums",".report.equity");
		Group g3 = new Group(2, GROUP3_ID, GroupType.USER, permittedFuncs);
		List<String> permittedSalesFuncs = ImmutableList.of(".report.sales",".report.clients");
		Group g4 = new Group(3, GROUP4_ID, GroupType.USER, permittedSalesFuncs);
		allGroups = ImmutableList.of(g1, g2, g3, g4);
	}
	
	public static enum GroupType { SUPER_USER, POWER_USER, USER };
	

	@Immutable
	public static class User {

		private final int id;
		private final String name;
		private final List<Integer> groupIds;
		
		private User(int id, String name, List<Integer> groupIds) {
			this.id = id;
			this.name = Preconditions.checkNotNull(name);
			if(groupIds == null) {
				this.groupIds = Collections.emptyList();
			} else {
				this.groupIds = groupIds;
			}
		}
		
		public String getName() {
			return name;
		}

		@Override public String toString() {
			return "User [id=" + id + ", name=" + name + ", groupIds=" + groupIds + "]";
		}
		
		
	}
	
	@Immutable
	public static class Group {

		private final int id;
		private final String name;
		private final GroupType groupType;
		private final List<String> permissionedEntities;
		
		private Group(int id, String name, GroupType groupType, List<String> permissionedEntities) {

			this.id = id;
			this.name = Preconditions.checkNotNull(name);
			this.groupType = Preconditions.checkNotNull(groupType);
			if(permissionedEntities == null) {
				this.permissionedEntities = Collections.emptyList();
			} else {
				this.permissionedEntities = permissionedEntities;
			}
		}
		
		public String getName() {
			return name;
		}
		
		public GroupType getGroupType() {
			return groupType;
		}
		
		public List<String> getPermissionedEntities() {
			return permissionedEntities;
		}

		@Override public String toString() {
			return "Group [id=" + id + ", name=" + name + ", groupType=" + groupType + "]";
		}
		
		
	}
}
