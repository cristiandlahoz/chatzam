package com.wornux.chatzam.data.entities;

import com.google.firebase.firestore.PropertyName;
import com.wornux.chatzam.data.enums.UserStatus;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Getter(onMethod_ = {@PropertyName("user_id")})
    @Setter(onMethod_ = {@PropertyName("user_id")})
    private String userId;
    
    @Getter(onMethod_ = {@PropertyName("email")})
    @Setter(onMethod_ = {@PropertyName("email")})
    private String email;
    
    @Getter(onMethod_ = {@PropertyName("display_name")})
    @Setter(onMethod_ = {@PropertyName("display_name")})
    private String displayName;
    
    @Getter(onMethod_ = {@PropertyName("profile_image_url")})
    @Setter(onMethod_ = {@PropertyName("profile_image_url")})
    private String profileImageUrl;
    
    @Getter(onMethod_ = {@PropertyName("is_online")})
    @Setter(onMethod_ = {@PropertyName("is_online")})
    private boolean isOnline;
    
    @Getter(onMethod_ = {@PropertyName("last_seen")})
    @Setter(onMethod_ = {@PropertyName("last_seen")})
    private Instant lastSeen;
    
    @Getter(onMethod_ = {@PropertyName("status")})
    @Setter(onMethod_ = {@PropertyName("status")})
    private UserStatus status;

    public void updateLastSeen() {
        this.lastSeen = Instant.now();
    }

    public void setOnlineStatus(boolean isOnline) {
        this.isOnline = isOnline;
        if (!isOnline) {
            updateLastSeen();
        }
    }
}