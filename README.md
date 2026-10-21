# Groupware Enhanced

팀 프로젝트로 개발한 그룹웨어를 기반으로, 기존에 담당했던 **인사등록 기능에 이미지 등록을 추가**하고 **DB 재설정, Spring Security 적용, CI/CD 재구축**으로 개선 범위를 넓히는 프로젝트입니다.

사원·부서 관리, 전자결재, 근태, 일정, 회의실 예약 등 사내 업무에 필요한 기능을 제공합니다. 이 문서는 기존 팀 프로젝트의 기능과 개인 담당 범위, 이번 고도화 작업을 구분하여 정리합니다.

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Backend | Java 17, Spring Framework 5.3.27 / Spring MVC |
| Database | Oracle, MyBatis 3.5.16 |
| Frontend | JSP, JSTL, JavaScript, jQuery, Bootstrap |
| UI 라이브러리 | DataTables, jsTree, FullCalendar |
| 실시간 통신 | Spring WebSocket |
| 빌드 및 실행 | Maven, WAR, Apache Tomcat 9 |
| 배포 관련 | 기존 Dockerfile 포함, CI/CD 재구축 예정 |
| 보안 고도화 | Spring Security 적용 예정 |

## 기존 팀 프로젝트 기능

다른 팀원이 담당한 주요 기능은 다음과 같습니다.

| 기능 | 주요 내용 |
| --- | --- |
| 로그인 | 로그인·로그아웃, 사원번호 찾기, 세션 기반 로그인 확인 |
| 전자결재 | 결재 문서 작성·조회, 결재선 및 참조자 설정, 결재 상태 관리, 양식·카테고리 관리, 전자서명 |
| 근태 관리 | 출근 처리, 근태 내역 및 연차 조회 |
| 일정 관리 | 캘린더 기반 일정 등록·조회 |
| 회의실 관리 | 회의실 등록·수정·조회, 예약 등록·조회·취소 |
| 알림 | WebSocket 기반 알림 처리 |

아래 개인 담당 기능 역시 기존 팀 프로젝트의 일부이며, 이번 작업은 해당 프로젝트를 이어서 개선하는 과정입니다.

## 기존 개인 담당 기능

Git 작성자 `kyerimrim`의 변경 이력과 현재 소스를 기준으로 정리했습니다.

### 1. 인사등록 및 사원 관리

- 사원 등록 화면과 등록 요청 처리 구현
- 입사일의 연·월과 순번을 조합한 사원번호 생성
- 부서, 직급, 이름, 연락처, 이메일, 입사일, 생년월일, 학력 등 사원 정보 입력 및 저장
- 사원 목록 및 개별 사원 조회
- DataTables를 활용한 사원 목록 표시와 검색
- 조직도 연동 및 조직도 검색 화면 구성

**주요 코드:** [HRManagementController](GroupWare/src/main/java/com/giga/gw/controller/HRManagementController.java), [Employee Mapper](GroupWare/src/main/resources/mapper/Employee_Mapper.xml), [사원 등록 화면](GroupWare/src/main/webapp/WEB-INF/views/employeeAdd.jsp), [인사 관리 화면](GroupWare/src/main/webapp/WEB-INF/views/hrManagement.jsp)

### 2. 부서 관리

- 본부·부서 목록 및 트리 조회
- 상위 조직을 선택한 부서 등록
- 부서명 중복 확인
- 부서 상세 조회 및 정보 수정
- 부서 관리 화면과 Controller·DAO·Mapper 연동

**주요 코드:** [DepartmentManagementController](GroupWare/src/main/java/com/giga/gw/controller/DepartmentManagementController.java), [DeptManagement Mapper](GroupWare/src/main/resources/mapper/DeptManagement_Mapper.xml), [부서 관리 화면](GroupWare/src/main/webapp/WEB-INF/views/deptManagement.jsp)

### 3. 마이페이지

- 로그인한 사원의 정보를 세션에서 가져와 화면에 표시
- 사번, 이름, 부서, 직급, 입사일, 내선번호, 이메일 조회
- 프로필 영역 및 개인 정보 조회 화면 구성

사원 등록 시 저장한 프로필 이미지를 마이페이지에서 조회하며, 미등록 시 로컬 기본 이미지를 표시합니다.

**주요 코드:** [마이페이지 화면](GroupWare/src/main/webapp/WEB-INF/views/mypage.jsp)

### 4. 공지사항 및 초기 화면 구성

- 공지사항 목록 조회 API와 화면 구현
- 작성자·제목·작성일 표시, 목록 검색 및 정렬
- 초기 메인 화면의 Grid 및 드래그 앤 드롭 화면 작업
- 공지사항의 메인 화면 적용 작업

**주요 코드:** [Notice Controller](GroupWare/src/main/java/com/giga/gw/controller/Notice_Controller.java), [공지사항 화면](GroupWare/src/main/webapp/WEB-INF/views/notice.jsp)

## 이번 추가 구현 및 고도화

기존 개인 담당 기능을 확장하는 작업과, 기존에 다른 팀원이 담당했던 영역을 이어받아 개선하는 작업으로 나눕니다. 체크된 항목은 코드 구현을 완료했으며, 실제 환경 검증 여부는 각 항목의 설명을 참고하세요.

### 1. 인사등록 — 사원 이미지 등록 추가

기존 사원 정보 등록 흐름에 프로필 이미지 등록을 연결합니다.

- [x] 사원 등록 화면에 이미지 선택 및 미리보기 추가
- [x] 이미지 업로드와 사원별 이미지 정보 저장 구현
- [x] 파일 형식 및 용량 검증
- [x] 등록한 이미지를 마이페이지에 표시
- [x] 이미지 미등록 시 기본 이미지 표시

JPG/PNG 이미지를 최대 5MiB, 1,600만 화소까지 허용합니다. 서버는 파일명이나 전송 MIME 타입 대신 실제 이미지 내용을 검사합니다. 이미지는 Base64 data URL로 `EMPLOYEES.PROFILE_IMAGE` CLOB에 저장되며, 사원 정보와 동일한 INSERT로 처리됩니다. 별도 업로드 디렉터리는 필요하지 않습니다.

**기존 DB는 배포 전에 [02_profile_image.sql](GroupWare/database/02_profile_image.sql)을 한 번 실행해야 합니다.** 새 DB에 최신 `01_schema.sql`을 적용했다면 추가 실행하지 않습니다. DB 적용 및 브라우저 통합 동작은 실행 환경에서 별도 확인이 필요합니다.

이미지 검증 및 multipart 등록 테스트: `mvn -f GroupWare/pom.xml -Dmaven.test.skip=false "-Dtest=ProfileImage*Test" test`

### 2. DB 재설정

기존 프로젝트를 로컬 환경에서 실행하고 기능을 검증할 수 있도록 DB 환경을 재구성합니다.

- [x] 로컬 Oracle 연결 설정 반영
- [x] DB 비밀번호를 `GROUPWARE_DB_PASSWORD` 환경변수로 분리
- [x] DTO·Mapper를 기준으로 복원용 스키마 스크립트 작성
- [ ] Oracle에서 스키마 생성 및 실행 결과 확인
- [ ] 부서·직급·사원 등 실행에 필요한 기초 데이터 구성
- [ ] 기존 Mapper와 복원 스키마의 호환성 및 주요 기능 동작 검증

복원용 스키마는 원본 DB 덤프가 아니며, 실제 DB 적용과 전체 기능 검증은 별도 단계입니다. 작성 범위와 알려진 문제는 [DB 복원 안내](GroupWare/database/README.md)에 정리되어 있습니다.

### 3. Spring Security 적용

기존 세션 및 `LoginFilter` 중심의 로그인 확인 구조에 Spring Security를 적용합니다.

- [ ] 로그인·로그아웃 및 인증 처리 통합
- [ ] 관리자와 일반 사원의 권한에 따른 접근 제어
- [ ] 비밀번호 해시 저장 및 검증 적용
- [ ] 기존 계정 데이터의 비밀번호 전환 방식 정리
- [ ] 세션 관리와 CSRF 보호를 기존 화면·AJAX 요청에 연동
- [ ] 인증 실패 및 권한 부족 시 응답 처리 검증

현재 소스에는 Spring Security가 적용되어 있지 않으며, 위 항목은 고도화 계획입니다.

### 4. CI/CD 재구축

현재 저장소의 소스를 기준으로 빌드·검증·배포 과정을 재구성합니다.

- [ ] 기존 Dockerfile의 원본 저장소 참조 및 DB Wallet 의존 설정 정리
- [ ] 현재 프로젝트의 Maven 빌드와 WAR 생성 자동화
- [ ] 테스트 실행 조건 및 DB가 필요한 테스트 환경 정리
- [ ] 배포 환경의 비밀값을 소스와 분리하여 주입
- [ ] 배포 자동화 및 배포 후 정상 동작 확인 단계 구성

CI/CD 도구와 배포 대상은 구축 과정에서 확정합니다.

## 프로젝트 구조

```text
Groupware-Enhanced/
├─ README.md
└─ GroupWare/
   ├─ pom.xml
   ├─ Dockerfile
   ├─ database/                 # 로컬 Oracle 복원 스크립트 및 안내
   └─ src/
      ├─ main/
      │  ├─ java/com/giga/gw/
      │  │  ├─ controller/     # 요청 처리
      │  │  ├─ service/        # 업무 로직
      │  │  ├─ repository/     # 데이터 접근
      │  │  ├─ dto/            # 데이터 전달 객체
      │  │  └─ filter/         # 기존 로그인 필터
      │  ├─ resources/
      │  │  ├─ mapper/         # MyBatis SQL
      │  │  ├─ mybatis/        # MyBatis 설정
      │  │  └─ properties/     # DB 연결 설정
      │  └─ webapp/
      │     ├─ WEB-INF/
      │     │  ├─ spring/      # Spring 설정
      │     │  └─ views/       # JSP 화면
      │     └─ resources/      # CSS, JavaScript 등
      └─ test/                 # 기존 JUnit 테스트
```

## 로컬 실행

1. JDK 17, Maven, Oracle, Tomcat 9 환경을 준비합니다.
2. [DB 복원 안내](GroupWare/database/README.md)에 따라 DB 스키마와 기초 데이터를 준비합니다. 스키마 생성만으로 로그인 계정이 만들어지지는 않습니다.
3. [DB 연결 설정](GroupWare/src/main/resources/properties/db.properties)을 실행 환경에 맞게 확인합니다.
   - 기본 연결: `localhost:1521/XEPDB1`
   - 기본 사용자: `GROUPWARE`
   - 비밀번호: 서버 실행 환경에 `GROUPWARE_DB_PASSWORD` 설정
4. 프로젝트 루트에서 빌드합니다.

   ```shell
   mvn -f GroupWare/pom.xml clean package
   ```

5. 생성된 `GroupWare/target/GroupWare.war`를 Tomcat에 배포합니다.
6. 기본 포트와 WAR 이름을 사용하는 경우 `http://localhost:8080/GroupWare/login.do`에 접속합니다.

현재 `pom.xml`은 기본적으로 테스트 컴파일과 실행을 건너뜁니다. 빌드 성공과 전체 기능 검증은 구분해야 하며, DB 연동 테스트는 실행 환경을 준비한 뒤 별도로 확인합니다.

기존 Dockerfile은 원본 팀 저장소와 Wallet 경로를 참조하므로, 이번 저장소의 로컬 실행 및 배포 설정에 맞춘 수정이 필요합니다.
