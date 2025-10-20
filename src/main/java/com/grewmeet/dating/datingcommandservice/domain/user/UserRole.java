package com.grewmeet.dating.datingcommandservice.domain.user;

import java.util.Set;

import static com.grewmeet.dating.datingcommandservice.domain.user.Entitlement.*;

public enum UserRole {
    
    GUEST(Set.of(CAN_PARTICIPATE)),
    HOST(Set.of(CAN_PARTICIPATE, CAN_CREATE, CAN_CANCEL)),
    ADMIN(Set.of(CAN_PARTICIPATE, CAN_CREATE, CAN_CANCEL, CAN_MANAGE_USERS)),
    BANNED(null);

    private final Set<Entitlement> capabilities;

    UserRole(Set<Entitlement> capabilities) {
        this.capabilities = capabilities;
    }

    public boolean can(Entitlement entitlement) {
        return capabilities.contains(entitlement);
    }

    public Set<Entitlement> getCapabilities() {
        return Set.copyOf(capabilities);
    }
}