package com.wornux.chatzam.data.entities;

import com.google.firebase.firestore.PropertyName;
import com.wornux.chatzam.data.enums.ChatType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupChat extends Chat {
    @Getter(onMethod_ = {@PropertyName("admin_users")})
    @Setter(onMethod_ = {@PropertyName("admin_users")})
    private List<String> adminUsers;
    
    @Getter(onMethod_ = {@PropertyName("group_description")})
    @Setter(onMethod_ = {@PropertyName("group_description")})
    private String groupDescription;
    
    @Getter(onMethod_ = {@PropertyName("max_participants")})
    @Setter(onMethod_ = {@PropertyName("max_participants")})
    private int maxParticipants;

    public GroupChat() {
        super();
        this.adminUsers = new ArrayList<>();
        this.setChatType(ChatType.GROUP);
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