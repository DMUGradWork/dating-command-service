# Dating Command Service

데이팅 도메인의 명령 서비스 (데이팅 모임 생성, 수정, 삭제, 참여/탈퇴)

## 🔧 환경 설정
- **포트**: 8080 (기본값)
- **데이터베이스**: H2 (인메모리)
- **Framework**: Spring Boot 3.5.3, Java 21

## 🏗️ 아키텍처
- **CQRS Command Side** (쓰기 전용, 복잡한 조회 로직 없음)
- **Outbox Pattern** (DB 트랜잭션 내 이벤트 기록, Kafka는 별도 Relay에서 발행)
- **Event-Driven Architecture** (UUID 기반 이벤트 통신)
- **Domain-Driven Design** 기반 구조

## 📡 이벤트 발행 (UUID 기반 EDA)
CQRS Command Side로서 모든 상태 변경 시 Outbox 패턴을 통해 이벤트 발행:

| 이벤트 타입 | Aggregate 타입 | 설명 | 발생 시점 |
|------------|---------------|------|----------|
| `DatingMeetingCreated` | DatingMeeting | 데이팅 모임 생성 | POST /events |
| `DatingMeetingUpdated` | DatingMeeting | 데이팅 모임 수정 | PATCH /events/{id} |
| `DatingMeetingDeleted` | DatingMeeting | 데이팅 모임 삭제 | DELETE /events/{id} |
| `DatingMeetingParticipantJoined` | DatingMeetingParticipant | 모임 참여 | POST /events/{id}/participants |
| `DatingMeetingParticipantLeft` | DatingMeetingParticipant | 모임 탈퇴 | DELETE /events/{id}/participants/{pid} |

### DatingMeetingCreated
```json
{
  "eventType": "DatingMeetingCreated",
  "aggregateType": "DatingMeeting",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
    "title": "서울 강남 데이팅 모임",
    "description": "강남에서 만나는 데이팅 모임입니다",
    "hostAuthUserId": "123e4567-e89b-12d3-a456-426614174000",
    "hostNickname": "홍길동",
    "meetingDateTime": "2025-01-20T19:00:00",
    "location": "강남역 스타벅스",
    "maxMaleParticipants": 5,
    "maxFemaleParticipants": 5,
    "createdAt": "2025-01-13T14:30:00"
  }
}
```

### DatingMeetingUpdated
```json
{
  "eventType": "DatingMeetingUpdated",
  "aggregateType": "DatingMeeting",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
    "title": "서울 강남 데이팅 모임 (수정)",
    "description": "강남에서 만나는 데이팅 모임입니다",
    "meetingDateTime": "2025-01-22T19:00:00",
    "location": "강남역 카페",
    "maxMaleParticipants": 4,
    "maxFemaleParticipants": 4,
    "updatedAt": "2025-01-13T15:30:00"
  }
}
```

### DatingMeetingDeleted
```json
{
  "eventType": "DatingMeetingDeleted",
  "aggregateType": "DatingMeeting",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
    "title": "서울 강남 데이팅 모임",
    "maxMaleParticipants": 5,
    "maxFemaleParticipants": 5,
    "deletedAt": "2025-01-13T16:00:00"
  }
}
```

### DatingMeetingParticipantJoined
```json
{
  "eventType": "DatingMeetingParticipantJoined",
  "aggregateType": "DatingMeetingParticipant",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
    "authUserId": "789e0123-e89b-12d3-a456-426614174001",
    "gender": "MALE",
    "meetingTitle": "서울 강남 데이팅 모임",
    "meetingDateTime": "2025-01-20T19:00:00",
    "joinedAt": "2025-01-13T17:00:00"
  }
}
```

### DatingMeetingParticipantLeft
```json
{
  "eventType": "DatingMeetingParticipantLeft",
  "aggregateType": "DatingMeetingParticipant",
  "aggregateId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
    "authUserId": "789e0123-e89b-12d3-a456-426614174001",
    "gender": "MALE",
    "leftAt": "2025-01-13T18:00:00"
  }
}
```

## 🚀 실행
```bash
./gradlew bootRun
```

## 📋 API 엔드포인트

### 데이팅 모임 관리
- **POST** `/events` - 새 데이팅 모임 생성 (Host만 가능)
  - Request Header: `X-User-Id` (UUID 형식 - Host 사용자 식별)
  - Request Body: `CreateDatingMeetingRequest` (title, description, meetingDateTime, location, maxMaleParticipants, maxFemaleParticipants)
  - Response: `DatingMeetingResponse` (모임 정보)

- **PATCH** `/events/{datingMeetingId}` - 모임 정보 부분 수정
  - Path Variable: `datingMeetingId` (Long - 내부 DB ID)
  - Request Body: `UpdateDatingMeetingRequest` (모든 필드 Optional)
  - Response: `DatingMeetingResponse` (수정된 모임 정보)

- **DELETE** `/events/{datingMeetingId}` - 모임 삭제
  - Path Variable: `datingMeetingId` (Long - 내부 DB ID)
  - Response: 204 No Content

### 참여자 관리
- **POST** `/events/{datingMeetingId}/participants` - 모임 참여
  - Path Variable: `datingMeetingId` (Long - 내부 DB ID)
  - Request Body: `JoinEventRequest` (userId - UUID)
  - Response: `ParticipantResponse` (참여자 정보)
  - 제약사항:
    - 성별별 정원 확인 (남/여 각각)
    - 중복 참여 불가
    - DatingUser 활성 상태 확인

- **DELETE** `/events/{datingMeetingId}/participants/{participantId}` - 모임 탈퇴
  - Path Variables: `datingMeetingId` (Long), `participantId` (Long)
  - Response: 204 No Content

## 📄 API 문서
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

## 🏗️ 아키텍처
- **CQRS Command Side** (쓰기 전용, 복잡한 조회 로직 없음)
- **Outbox Pattern** (DB 트랜잭션 내 이벤트 기록)
- **Event-Driven Architecture** (Kafka 기반 이벤트 발행)
- **Domain-Driven Design** 기반 구조

## 🔒 핵심 제약사항
- 모든 서비스 메서드는 `@Transactional` 필수
- 복잡한 조회 기능은 수행하지 않음 (Query Service 역할 아님)
- 상태 변경과 Outbox 이벤트 기록의 원자성 보장

---

## 📝 TODO: 향후 개선 작업 목록

### ✅ 완료된 작업 (Completed)
- [x] DatingUser 엔티티 추가 및 관계 설정 (Gender enum, Quarter 관리 포함)
- [x] 성별 기반 정원 관리 시스템 (`maxMaleParticipants`, `maxFemaleParticipants`)
- [x] Host User 개념 도입 (hostDatingUserId 필드)
- [x] 성별 기반 정원 검증 로직 구현
- [x] 참가자 중복 검증 및 비즈니스 룰 강화
- [x] UUID 기반 Event-Driven Architecture 전환
- [x] 이벤트 페이로드 UUID 기반 업데이트
- [x] Outbox Pattern aggregateId를 String으로 변경

### 🔄 우선순위 높음 (High Priority)

#### 1. Outbox Relay 구현
- [ ] Outbox 테이블에서 PENDING 이벤트를 읽어 Kafka로 발행하는 별도 프로세스 구현
- [ ] 발행 성공 시 Outbox 상태를 PROCESSED로 변경
- [ ] 발행 실패 시 재시도 로직 및 FAILED 상태 처리
- [ ] 배치 처리 및 트랜잭션 관리

#### 2. 테스트 케이스 추가
- [ ] 성별별 정원 초과 시 예외 처리 테스트
- [ ] DatingUser 미존재 시 예외 처리 테스트
- [ ] Host 권한 검증 테스트 (수정/삭제)
- [ ] 참여자 탈퇴 처리 테스트
- [ ] 통합 테스트 케이스 추가

### 🧪 우선순위 중간 (Medium Priority)

#### 3. 에러 처리 개선
- [ ] 전역 예외 처리 (@ControllerAdvice)
- [ ] 에러 응답 메시지 표준화
- [ ] 비즈니스 예외 클래스 계층 구조 정립
- [ ] HTTP Status 코드 일관성 검토

#### 4. 고급 기능 (선택사항)
- [ ] 참가 요청/승인 프로세스 도입
- [ ] Vote 시스템 구현
- [ ] 참가자 매칭 알고리즘
- [ ] 지역 기반 필터링
- [ ] 나이 제한 기능

### 🔧 인프라 및 운영 (Low Priority)
- [ ] 데이터베이스 마이그레이션 스크립트 작성
- [ ] 성능 테스트 (특히 성별별 집계 쿼리)
- [ ] 모니터링 메트릭 추가
- [ ] 로깅 전략 개선

---

> **참고**: UUID 기반 EDA 전환 완료. DatingUser/Host 개념 및 성별별 정원 관리 시스템 구현 완료.
> 현재 Outbox Relay 구현이 핵심 우선순위입니다.