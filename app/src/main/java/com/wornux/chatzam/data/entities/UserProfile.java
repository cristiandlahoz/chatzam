package com.wornux.chatzam.data.entities;

import com.wornux.chatzam.data.enums.UserStatus;
import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String userId;
    private String email;
    private String displayName;
    private String profileImageUrl;
    private boolean isOnline;
    private Date lastSeen;
    private UserStatus status;

    public void updateLastSeen() {
        this.lastSeen = new Date();
    }

    public void setOnlineStatus(boolean isOnline) {
        this.isOnline = isOnline;
        if (!isOnline) {
            updateLastSeen();
        }
    }
}