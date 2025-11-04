# Dating Command Service

데이팅 도메인의 명령 서비스 (데이팅 모임 생성, 수정, 삭제, 참여/탈퇴)

## 🔧 환경 설정
- **포트**: 8085
- **데이터베이스**: MySQL 8.0
- **Framework**: Spring Boot 3.5.3, Java 21
- **메시지 큐**: Kafka (localhost:9092)

## 🏗️ 아키텍처
- **CQRS Command Side** (쓰기 전용)
- **Event-Driven Architecture** (Kafka 직접 발행)
- **DatingEventPublisher** (이벤트 발행 전담)
- **Domain-Driven Design** 기반 구조

## 📡 이벤트 발행 (Kafka Topics)

| 이벤트 타입 | Kafka Topic | 설명 |
|------------|-------------|------|
| `DatingMeetingCreated` | `grewmeet.dating.meeting.created` | 미팅 생성 |
| `DatingMeetingUpdated` | `grewmeet.dating.meeting.updated` | 미팅 수정 |
| `DatingMeetingDeleted` | `grewmeet.dating.meeting.deleted` | 미팅 삭제 |
| `DatingMeetingParticipantJoined` | `grewmeet.dating.participant.joined` | 참여자 가입 |
| `DatingMeetingParticipantLeft` | `grewmeet.dating.participant.left` | 참여자 탈퇴 |

### 이벤트 스키마

**DatingMeetingCreated**
```json
{
  "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
  "title": "인코딩 테스트 미팅",
  "description": "한글 확인",
  "hostAuthUserId": "550e8400-e29b-41d4-a716-446655440000",
  "hostNickname": "테스트호스트",
  "meetingDateTime": "2025-12-01T19:00:00",
  "location": "서울시 강남구",
  "maxMaleParticipants": 5,
  "maxFemaleParticipants": 5,
  "currentMaleParticipants": 0,
  "currentFemaleParticipants": 0,
  "createdAt": "2025-11-04T18:14:48.482233"
}
```

**DatingMeetingUpdated**
```json
{
  "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
  "title": "수정된 제목",
  "description": "수정된 설명",
  "meetingDateTime": "2025-12-15T20:00:00",
  "location": "서울시 종로구",
  "maxMaleParticipants": 7,
  "maxFemaleParticipants": 7,
  "currentMaleParticipants": 1,
  "currentFemaleParticipants": 1,
  "updatedAt": "2025-11-04T18:23:28.061821"
}
```

**DatingMeetingDeleted**
```json
{
  "meetingUuid": "550e8400-e29b-41d4-a716-446655440000",
  "deletedAt": "2025-11-04T18:35:00"
}
```

## 🚀 실행
```bash
./gradlew bootRun
```

## 📋 API 엔드포인트

### 데이팅 모임 관리
- **POST** `/events` - 새 데이팅 모임 생성
  - Header: `X-User-Id` (UUID - Host 사용자)
  - Body: `CreateDatingMeetingRequest`
  - Response: `DatingMeetingResponse` (id, meetingUuid 포함)

- **PATCH** `/events/{id}` - 모임 정보 수정
  - Path: `id` (Long - DB 내부 ID)
  - Body: `UpdateDatingMeetingRequest`

- **DELETE** `/events/{id}` - 모임 삭제
  - Path: `id` (Long - DB 내부 ID)

### 참여자 관리
- **POST** `/events/{id}/participants` - 모임 참여
  - Header: `X-User-Id` (UUID)
  - 성별별 정원 확인

- **DELETE** `/events/{id}/participants/{participantId}` - 모임 탈퇴

## 📄 API 문서
- Swagger UI: http://localhost:8085/swagger-ui.html

## 🗄️ 데이터베이스
- MySQL 8.0 (UTF-8 인코딩)
- 테이블: `dating_meetings`, `dating_users`, `participants`

## 🔑 핵심 특징
- **이중 ID 시스템**
  - `id` (Long): 내부 DB 식별자
  - `meetingUuid` (UUID): 외부 API 및 이벤트 식별자
- **성별별 정원 관리** (남/여 독립 관리)
- **한글 UTF-8 완벽 지원**
- **현재 참여 인원수 실시간 반영**
