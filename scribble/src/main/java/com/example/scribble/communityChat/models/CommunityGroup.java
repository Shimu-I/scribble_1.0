package com.example.scribble.communityChat.models;

public class CommunityGroup {
    private int groupId;
    private String groupName;

    public CommunityGroup(int groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return groupName;  // So ListView displays just the group name
    }
}
