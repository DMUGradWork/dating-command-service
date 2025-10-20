package com.grewmeet.dating.datingcommandservice.domain.user;

public enum Entitlement {
    CAN_PARTICIPATE,    // 이벤트 참여 권한
    CAN_CREATE,         // 이벤트 생성 권한
    CAN_CANCEL,         // 이벤트 취소 권한
    CAN_MANAGE_USERS    // 사용자 관리 권한 (Admin용)
}