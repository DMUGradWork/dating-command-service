# Dating Command Service: AI 개발 가이드

이 문서는 `dating-command-service`의 역할, 아키텍처, 개발 가이드라인을 정의합니다.

## 1. 내 핵심 책임 (My Core Responsibility)

나는 **`Dating Event`의 상태를 변경(Create, Update, Delete, 참여, 탈퇴)하는 Command 서비스**입니다. 나의 유일한 임무는 데이터의 일관성을 유지하며 쓰기 요청을 처리하는 것입니다. 복잡한 조회 기능은 수행하지 않습니다.

---

## 2. 내 API 엔드포인트 (My API Endpoints)

* `POST /events`: 새 데이팅 이벤트 생성
* `PUT /events/{eventId}`: 이벤트 정보 수정
* `DELETE /events/{eventId}`: 이벤트 삭제
* `POST /events/{eventId}/participants`: 이벤트 참여
* `DELETE /events/{eventId}/participants/{participantId}`: 이벤트 탈퇴

---

## 3. 기술 아키텍처 및 제약사항 (Tech & Constraints)

* **CQRS Command Side**: 나는 CQRS 패턴의 Command 측면을 담당하며, 데이터를 조회하는 복잡한 로직은 절대 포함해서는 안 됩니다.
* **Outbox Pattern & Event-Driven**: 모든 상태 변경은 **DB 트랜잭션 내에서 `outbox` 테이블에 이벤트를 기록하는 것으로 완료**됩니다. Kafka에 직접 이벤트를 발행하는 코드는 내부에 없습니다.
* **트랜잭션 관리**: 모든 public 서비스 메서드는 `@Transactional`을 사용하여 데이터 변경과 Outbox 이벤트 기록의 원자성을 보장해야 합니다.

---

## 4. 프로젝트 개요 및 기술 스택

* **Project**: `dating-command-service`
* **Package**: `com.grewmeet.dating.datingcommandservice`
* **Framework**: Spring Boot 3.5.3, Java 21, Gradle
* **Key Dependencies**: Spring Web, Data JPA, Security, Validation, Lombok
* **Database**: H2 (개발용)

---

## 5. 공통 개발 명령어 (Common Commands)

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 전체 테스트 실행
./gradlew test

# 개발 모드로 실행 (Hot Reload)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 클린 빌드
./gradlew clean build
```

---

## 6. 도메인 모델링 가이드라인 (Domain Modeling)

### Entity 설계 원칙
* **Event**: 데이팅 이벤트의 핵심 정보 (제목, 설명, 일시, 장소, 최대참여자수)
* **Participant**: 이벤트 참여자 정보 (사용자ID, 참여일시, 상태)
* **Outbox**: 이벤트 발행을 위한 아웃박스 패턴 구현

### DTO 설계 원칙
* **Request DTO**: 클라이언트 요청 검증을 위한 Bean Validation 적용
* **Response DTO**: 최소한의 정보만 포함, 민감정보 제외
* **Event DTO**: 다른 서비스로 발행할 이벤트 데이터 구조

---

## 7. 에러 처리 및 검증 전략 (Error Handling)

### 표준 예외 처리
* **Business Exception**: 비즈니스 규칙 위반 시 사용
* **@ControllerAdvice**: 전역 예외 처리기로 일관된 에러 응답
* **HTTP Status**: 적절한 상태 코드 반환 (400, 404, 409, 500)

### 검증 규칙
* **@Valid**: 요청 DTO 검증
* **@Transactional**: 모든 서비스 메서드에 필수 적용
* **낙관적 락**: 동시성 제어를 위한 @Version 사용

---

## 8. 테스트 전략 (Testing Strategy)

### 테스트 계층
* **Unit Test**: 서비스 로직 검증, Mock 사용
* **Integration Test**: Repository와 DB 연동 테스트
* **API Test**: 컨트롤러 엔드포인트 테스트 (@WebMvcTest)

### 테스트 데이터 관리
* **@Sql**: 테스트용 데이터 setup/cleanup
* **TestContainers**: 실제 DB 환경과 유사한 통합 테스트
* **@Transactional**: 테스트 격리를 위한 롤백

```bash
# 특정 테스트 클래스 실행
./gradlew test --tests "EventServiceTest"

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```