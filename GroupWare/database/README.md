# 로컬 Oracle 스키마 복원

`01_schema.sql`은 DTO 16개와 Mapper 14개를 대조해 작성한 **복원용 초기 스키마**입니다. 원본 DDL이나 DB 덤프가 아니므로 원본과 동일한 구조라고 보장하지 않습니다.

## 실행 방법

1. DBeaver에서 **GROUPWARE 계정**, 서비스 이름 **XEPDB1** 연결을 선택합니다. SYSTEM 연결에서 실행하지 않습니다.
2. SQL 편집기에서 다음을 실행해 접속 대상을 확인합니다.

   ```sql
   SELECT USER, SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS CURRENT_SCHEMA,
          SYS_CONTEXT('USERENV', 'CON_NAME') AS CON_NAME FROM DUAL;
   SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME;
   ```

3. 이 스크립트는 빈 프로젝트 스키마에 최초 한 번 실행하는 용도입니다. 이미 테이블이 있다면 먼저 구조를 비교하세요. 기존 테이블을 삭제하지 않습니다.
4. `00_check_connection.sql`로 위 접속 정보와 빈 테이블 목록을 확인한 다음, `01_schema.sql`을 해당 연결의 SQL 편집기로 열고 **SQL 스크립트 실행**으로 실행합니다. 생성 파일에는 DBeaver 구분자 오류를 피하기 위해 BEGIN/END 검사 블록과 단독 `/`가 없습니다. 잘못된 계정에서의 실행을 자동 차단하지 않으므로 접속 결과를 먼저 확인해야 합니다. DBeaver의 오류 처리에서는 **중단**을 선택하세요.
5. 성공하면 Schemas → GROUPWARE → Tables를 새로고침합니다. 테이블 19개, 명시적 시퀀스 10개가 생성됩니다. ATTENDANCE의 identity용 시스템 시퀀스가 추가로 보이는 것은 정상입니다.

Oracle DDL은 자동 커밋됩니다. 중간 실패 시 앞서 생성된 객체는 남습니다. 전체 스크립트를 무작정 재실행하지 말고 실패한 구문과 현재 객체를 확인하세요.

## 복원 범위와 설계 가정

- 사원/조직: DEPARTMENTS, JOBS, EMPLOYEES, EMP_IMAGES
- 근태/일정: ATTENDANCE, ATTENDANCERECORD, SCHEDULE
- 전자결재: APPROVAL_CATEGORY, APPROVAL_FORM, APPROVAL, APPROVAL_LINE, APPROVAL_REF, SAVE_LINE
- 회의실: ROOMS, RESERVATION, RESERVATION_PARTICIPANT
- 게시판/파일/공통코드: NOTICE, TBL_FILE, TBL_COMM
- PageDto는 페이지 계산용이므로 테이블이 없습니다. LoginDto도 현재 Mapper가 EMPLOYEES를 조회하므로 별도 로그인 테이블을 만들지 않습니다.
- DTO의 deptname/job_title/name/dname/parent_deptname 등 조인 결과 필드와 결재선·참조자 목록은 해당 원본 테이블/자식 테이블로 분리했습니다. RESERVATION.ROOM_NAME은 기존 INSERT에서 사용하므로 유지했습니다.
- PASSWORD, HR_STATUS처럼 DTO에 없어도 Mapper가 사용하는 컬럼을 포함했습니다. PASSWORD 255자는 향후 해시 저장 공간을 확보한 것이며, 현재 로그인 SQL의 평문 비교 방식 자체를 변경하지는 않습니다.
- 길이, NULL 허용, FK, 기본값은 추정입니다. USE_YN은 Y, 생성일은 SYSDATE, TEMP_SAVE_YN은 N으로 두어 값을 생략하는 기존 INSERT에 대응했습니다. 감사 컬럼에는 초기 관리자 데이터 의존성을 피하기 위해 FK를 넣지 않았습니다.
- EMPNO는 현재 NUMERIC 바인딩과 조직도 TO_CHAR 사용에 맞춰 NUMBER(10)으로 통일했습니다. 기존 사번은 7자리 숫자입니다. 선행 0 또는 문자 사번을 지원하려면 Java DTO와 SQL을 함께 변경해야 합니다.
- FORM_CONTENT/APPROVAL_CONTENT/서명/공지 본문은 CLOB입니다. SAVE_LINE.LINE_DATA 및 SCHEDULE.SCH_CONTENT는 현재 Map 조회와 문자열 소비를 고려해 VARCHAR2(4000 BYTE)로 두었습니다. 한글은 바이트 제한에 더 빨리 도달하며, 더 큰 데이터는 CLOB 매핑 개선이 필요합니다.
- SCHEDULE 시작·종료일은 변환 없이 전달되는 FullCalendar 문자열을 보존하기 위해 VARCHAR2(50 CHAR)를 사용합니다. 다른 날짜 연산 대상 컬럼은 DATE입니다.
- ATTENDANCE의 ID identity, TBL_COMM.CODE_NAME, RESERVATION_PARTICIPANT.EMPNO와 복합 PK는 원본에서 확인되지 않은 복원 설계입니다. Mapper는 TBL_COMM.CODE와 참여자 테이블의 RESERVATION_ID만 참조합니다.
- 기존 코드와 맞추기 위해 ADRESS 오타, SEQUENCE/MEMBER/DEGREE의 대문자 컬럼 이름을 유지했습니다.
- 기존 MAX(id)+1 로직과 LPAD 자릿수 제한은 이 스크립트로 해결되지 않습니다. 동시 등록 충돌 및 자릿수 초과 문제는 Mapper 개선이 필요합니다.

## 다음 단계 / 알려진 기존 코드 문제

이 스크립트에는 사용자 생성, 비밀번호, 샘플 사원, 부서, 직급, 공통코드 등의 INSERT가 없습니다. 테이블 생성만으로 로그인되지는 않습니다. 기초 데이터는 부서·직급 → 사원 → 근태/결재양식 등의 순서로 별도로 준비해야 합니다. 근태의 연차 처리에는 TBL_COMM의 AT05 코드가 필요합니다.

Mapper 점검 중 발견한 아래 문제는 테이블 생성과 별개이며 이번 작업에서 수정하지 않았습니다.

- Calendar_Mapper.xml `loadDeptSchedule`: `DEPTNO = #{deptno} s.USE_YN = 'Y'` 사이에 AND가 없습니다.
- Employee/Room/DeptManagement Mapper에 DATE를 다시 TO_DATE로 변환하거나 날짜 문자열을 암묵 변환하는 SQL이 있습니다. Approval/Reservation도 String을 DATE에 직접 바인딩합니다. Oracle 세션의 NLS_DATE_FORMAT에 따라 실패할 수 있으므로 명시적 날짜 바인딩/변환으로 정리해야 합니다. DBeaver의 ALTER SESSION 설정은 Tomcat 연결에 적용되지 않습니다.
- ApprovalForm_Mapper.xml의 formJoinCategory는 FORM_CONTENT에 CATEGORY_NAME을 매핑합니다. 본문 조회 의도와 맞는지 확인이 필요합니다.
- ATTNO_SEQ 재생성과 고정 길이 ID 생성, 테스트용 하드코딩 사번/문서 번호가 남아 있습니다. 운영용 동작은 별도 확인이 필요합니다.

## 검증 범위

활성 Mapper의 테이블 참조, NEXTVAL 시퀀스 참조 및 INSERT 컬럼을 생성 스크립트와 정적 대조했습니다. 테이블 19개와 명시적 시퀀스 10개가 포함됩니다. 실제 Oracle에서 DDL을 실행하거나 앱 전체 기능을 검증한 상태는 아닙니다. 기존 DB에는 어떤 변경도 실행하지 않았습니다.
