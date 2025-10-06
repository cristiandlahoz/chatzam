package com.wornux.chatzam.data.entities;

import com.google.firebase.firestore.PropertyName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @Getter(onMethod_ = {@PropertyName("user_id")})
    @Setter(onMethod_ = {@PropertyName("user_id")})
    private String userId;
    
    @Getter(onMethod_ = {@PropertyName("display_name")})
    @Setter(onMethod_ = {@PropertyName("display_name")})
    private String displayName;
    
    @Getter(onMethod_ = {@PropertyName("profile_image_url")})
    @Setter(onMethod_ = {@PropertyName("profile_image_url")})
    private String profileImageUrl;
    
    @Getter(onMethod_ = {@PropertyName("last_seen")})
    @Setter(onMethod_ = {@PropertyName("last_seen")})
    private Date lastSeen;
    
    @Getter(onMethod_ = {@PropertyName("is_online")})
    @Setter(onMethod_ = {@PropertyName("is_online")})
    private boolean isOnline;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return userId != null && userId.equals(userDTO.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
