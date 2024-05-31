# POSweb
## Notion Link : https://magic-walnut-fef.notion.site/POS-web-73333b1a4934410683bb70c2c5199d6a?pvs=4

## 🖥️ 프로젝트 소개
Spring Boot + JPA + thymeleaf를 활용한 서버 사이드 개발 포스 프로그램

## 👪 개발 기간
* 24.04.23 화 ~ 24.05.29 수

## 💿 사용 기능
- Entity별 Dto를 사용하여 클라이언트에게 entity 노출 방지
- Fetch Join을 사용하여 데이터를 한 번에 조회
- JPA Grouping을 이용한 합계, 평균 조회
- IamPort API를 활용한 결제 & 결제 취소 연동
- Valid 어노테이션을 활용한 회원가입 form에 대한 유효성 검증
- 다음 주소 API를 활용한 주소 검색
- Spring Security를 활용한 사용자 인증 & 인가
- Custom Handler를 사용한 로그인 & 로그아웃 시 추가 로직 처리
- Page 객체를 사용한 Paging 처리
- fetch 통신을 이용한 Rest API 통신 - 아이디 중복 확인
- PasswordEncoder를 사용한 비밀번호 암호화 & match

## 💾 사용 기술
#### `IDE`
- IntelliJ IDE 2023.2.5
#### `Back-end`
- Spring Boot '3.2.5'
- Spring Data JPA
- Spring Validation
- Spring Security 6
- Jackson Data Hibernate 6
- Java '17'
- Lombok
- Spring Test
- Junit '4.12'
#### `Front-end`
- HTML
- thymeleaf
- JavaScript
- Bootstrap

## 📑 테이블 구조
### ![image](https://github.com/JIGeons/POSweb/assets/118729956/90309626-6c8a-4a1c-be78-0b040656b529)
### ![image](https://github.com/JIGeons/POSweb/assets/118729956/41bc25a2-771f-40e0-bc42-fec05feba85f)

## 💙 핵심 기능
### 이 프로젝트의 핵심 기능은 상품을 구매하고 주문 조회 & 취소입니다.<br>부가 서비스로 사용자가 로그인 & 로그아웃을 할 시 해당 시간을 기록 하여 사용자의 급여를 보다 쉽게 알 수 있도록 했습니다.</br>

### Iamport API


### 주문 조회 & 취소


### 로그인 & 로그아웃 Handler


## 🤎 시스템구성도
![시스템 구성도](https://github.com/JIGeons/POSweb/assets/118729956/1103b8f4-0aba-442a-98b7-d6af79c0cb9e)

