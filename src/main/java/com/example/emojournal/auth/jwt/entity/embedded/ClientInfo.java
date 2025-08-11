package com.example.emojournal.auth.jwt.entity.embedded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

    private String userAgent;

    private String ipAddress;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientInfo)) return false;
        ClientInfo that = (ClientInfo) o;
        return Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(ipAddress, that.ipAddress);
    }


    @Override
    public int hashCode() {
        return Objects.hash(userAgent, ipAddress);
    }
}
