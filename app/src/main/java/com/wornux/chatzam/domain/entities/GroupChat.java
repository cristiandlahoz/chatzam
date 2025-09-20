package com.wornux.chatzam.domain.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChat extends Chat {
    private List<String> adminUsers;
    private String groupDescription;
    private int maxParticipants;

    public GroupChat() {
        super();
        this.adminUsers = new ArrayList<>();
        this.setGroup(true);
        this.maxParticipants = 256;
    }

    public void addAdmin(String userId) {
        if (!adminUsers.contains(userId)) {
            adminUsers.add(userId);
        }
    }

    public void removeAdmin(String userId) {
        adminUsers.remove(userId);
    }

    public boolean isAdmin(String userId) {
        return adminUsers.contains(userId);
    }
}