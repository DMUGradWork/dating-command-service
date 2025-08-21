# Dating Command Service

데이팅 도메인의 명령 서비스 (데이팅 이벤트 생성, 수정, 삭제, 참여/탈퇴)

## 🔧 환경 설정
- **포트**: 8080 (기본값)
- **데이터베이스**: H2 (인메모리)
- **Kafka**: localhost:9092

## 📡 이벤트 발행
CQRS Command Side로서 모든 상태 변경 시 Outbox 패턴을 통해 이벤트 발행:

| 이벤트 타입 | Aggregate 타입 | 설명 | 발생 시점 |
|------------|---------------|------|----------|
| `DatingMeetingCreated` | DatingMeeting | 데이팅 이벤트 생성 | POST /events |
| `DatingMeetingUpdated` | DatingMeeting | 데이팅 이벤트 수정 | PATCH /events/{id} |
| `DatingMeetingDeleted` | DatingMeeting | 데이팅 이벤트 삭제 | DELETE /events/{id} |
| `ParticipantJoined` | Participant | 이벤트 참여 | POST /events/{id}/participants |
| `ParticipantLeft` | Participant | 이벤트 탈퇴 | DELETE /events/{id}/participants/{pid} |

### DatingMeetingCreated
```json
{
  "eventType": "DatingMeetingCreated",
  "aggregateType": "DatingMeeting",
  "aggregateId": 123,
  "data": {
    "id": 123,
    "title": "서울 강남 데이팅 모임",
    "description": "강남에서 만나는 데이팅 모임입니다",
    "meetingDateTime": "2024-08-21T19:00:00",
    "location": "강남역 스타벅스",
    "maxParticipants": 10,
    "createdAt": "2024-08-21T14:30:00"
  }
}
```

### DatingMeetingUpdated
```json
{
  "eventType": "DatingMeetingUpdated",
  "aggregateType": "DatingMeeting",
  "aggregateId": 123,
  "data": {
    "id": 123,
    "title": "서울 강남 데이팅 모임 (수정)",
    "description": "강남에서 만나는 데이팅 모임입니다",
    "meetingDateTime": "2024-08-22T19:00:00",
    "location": "강남역 카페",
    "maxParticipants": 8,
    "updatedAt": "2024-08-21T15:30:00"
  }
}
```

### DatingMeetingDeleted
```json
{
  "eventType": "DatingMeetingDeleted",
  "aggregateType": "DatingMeeting",
  "aggregateId": 123,
  "data": {
    "id": 123,
    "title": "서울 강남 데이팅 모임",
    "participantIds": [1, 2, 3],
    "deletedAt": "2024-08-21T16:00:00"
  }
}
```

### ParticipantJoined
```json
{
  "eventType": "ParticipantJoined",
  "aggregateType": "Participant",
  "aggregateId": 456,
  "data": {
    "eventId": 123,
    "participantId": 456,
    "userId": "user_987654321",
    "joinedAt": "2024-08-21T17:00:00"
  }
}
```

### ParticipantLeft
```json
{
  "eventType": "ParticipantLeft",
  "aggregateType": "Participant",
  "aggregateId": 456,
  "data": {
    "eventId": 123,
    "participantId": 456,
    "userId": "user_987654321",
    "leftAt": "2024-08-21T18:00:00"
  }
}
```

## 🚀 실행
```bash
./gradlew bootRun
```

## 📋 API 엔드포인트

### 이벤트 관리
- **POST** `/events` - 새 데이팅 이벤트 생성
- **PATCH** `/events/{eventId}` - 이벤트 정보 부분 수정
- **DELETE** `/events/{eventId}` - 이벤트 삭제

### 참여자 관리
- **POST** `/events/{eventId}/participants` - 이벤트 참여
- **DELETE** `/events/{eventId}/participants/{participantId}` - 이벤트 탈퇴

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