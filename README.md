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
| `DatingMeetingParticipantJoined` | DatingMeetingParticipant | 이벤트 참여 | POST /events/{id}/participants |
| `DatingMeetingParticipantLeft` | DatingMeetingParticipant | 이벤트 탈퇴 | DELETE /events/{id}/participants/{pid} |

### DatingMeetingCreated
```json
{
  "eventType": "DatingMeetingCreated",
  "aggregateType": "DatingMeeting",
  "aggregateId": 123,
  "data": {
    "datingMeetingId": 123,
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
    "datingMeetingId": 123,
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
    "datingMeetingId": 123,
    "title": "서울 강남 데이팅 모임",
    "participantIds": [1, 2, 3],
    "deletedAt": "2024-08-21T16:00:00"
  }
}
```

### DatingMeetingParticipantJoined
```json
{
  "eventType": "DatingMeetingParticipantJoined",
  "aggregateType": "DatingMeetingParticipant",
  "aggregateId": 456,
  "data": {
    "datingMeetingId": 123,
    "participantId": 456,
    "userId": 987654321,
    "joinedAt": "2024-08-21T17:00:00"
  }
}
```

### DatingMeetingParticipantLeft
```json
{
  "eventType": "DatingMeetingParticipantLeft",
  "aggregateType": "DatingMeetingParticipant",
  "aggregateId": 456,
  "data": {
    "datingMeetingId": 123,
    "participantId": 456,
    "userId": 987654321,
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
- **PATCH** `/events/{datingMeetingId}` - 이벤트 정보 부분 수정
- **DELETE** `/events/{datingMeetingId}` - 이벤트 삭제

### 참여자 관리
- **POST** `/events/{datingMeetingId}/participants` - 이벤트 참여
- **DELETE** `/events/{datingMeetingId}/participants/{participantId}` - 이벤트 탈퇴

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

## 📝 TODO: 도메인 모델 개선 작업 목록

### 🚨 우선순위 높음 (Core Domain Issues)

#### 1. User 엔티티 추가 및 관계 설정
- [ ] User 엔티티 생성 (id, username, gender, role, status 등)
- [ ] Gender enum 추가 (MALE, FEMALE)
- [ ] UserRole enum 추가 (USER, ADMIN 등)
- [ ] Participant와 User 간 @ManyToOne 관계 설정
- [ ] 기존 `Long userId` → `User user`로 변경
- [ ] User Repository 및 Service 구현

#### 2. 성별 기반 정원 관리 시스템
- [ ] DatingMeeting에 `maxMaleParticipants`, `maxFemaleParticipants` 필드 추가
- [ ] 기존 `maxParticipants` → 성별별 정원으로 분리
- [ ] 성별별 정원 초과 검증 로직 구현
- [ ] `getMaleParticipantsCount()`, `getFemaleParticipantsCount()` 메서드 추가
- [ ] `isMaleFull()`, `isFemaleFull()` 검증 메서드 구현

#### 3. Host User 개념 도입
- [ ] DatingMeeting에 `hostUser` 필드 추가
- [ ] 이벤트 생성 시 Host 지정 로직
- [ ] Host 권한 검증 (수정/삭제는 Host만 가능)
- [ ] CreateDatingMeetingRequest에 hostUserId 추가

### 🔄 우선순위 중간 (Business Logic Enhancement)

#### 4. 참가자 검증 로직 강화
- [ ] 중복 참가 검증 강화 (현재 기본 구현됨)
- [ ] 성별 기반 정원 검증 구현
- [ ] 사용자별 동시 참가 제한 (최대 3개) 구현
- [ ] User 활성 상태 검증 추가
- [ ] `canAcceptParticipation()` 메서드 구현
- [ ] `User.canRequest()` 검증 로직 구현

#### 5. API 및 DTO 업데이트
- [ ] CreateDatingMeetingRequest에 성별별 정원 필드 추가
- [ ] UpdateDatingMeetingRequest에 성별별 정원 필드 추가
- [ ] User 생성/조회 API 추가 (선택사항)
- [ ] 에러 응답 메시지 개선 및 표준화
- [ ] Swagger 문서 업데이트

### 🧪 우선순위 낮음 (Testing & Advanced Features)

#### 6. 테스트 케이스 추가
- [ ] 성별별 정원 초과 시 예외 처리 테스트
- [ ] User 미존재 시 예외 처리 테스트  
- [ ] 중복 참가 방지 테스트 강화
- [ ] Host 권한 테스트 (수정/삭제)
- [ ] 사용자별 참가 제한 테스트
- [ ] 통합 테스트 케이스 추가

#### 7. 이벤트 페이로드 업데이트
- [ ] User 정보 포함하도록 이벤트 구조 개선
- [ ] 성별 정보 포함한 참가자 이벤트
- [ ] Host 정보 포함한 미팅 생성 이벤트
- [ ] 이벤트 스키마 버전 관리

#### 8. 고급 기능 (선택사항)
- [ ] 참가 요청/승인 프로세스 도입
- [ ] Vote 시스템 구현
- [ ] 참가자 매칭 알고리즘
- [ ] 지역 기반 필터링
- [ ] 나이 제한 기능

### 🔧 인프라 및 운영
- [ ] 데이터베이스 마이그레이션 스크립트 작성
- [ ] 기존 데이터 호환성 검토
- [ ] 성능 테스트 (특히 성별별 집계 쿼리)
- [ ] 모니터링 메트릭 추가

---

> **참고**: 이 작업들은 현재 과도하게 단순화된 도메인 모델을 실제 데이팅 서비스에 적합한 수준으로 개선하기 위한 것입니다. 
> 우선순위에 따라 단계적으로 진행하되, Core Domain Issues부터 먼저 해결하는 것을 권장합니다.