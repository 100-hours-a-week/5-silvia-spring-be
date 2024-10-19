## 🥑 AvoWorld 🥑
![readme-003](https://github.com/user-attachments/assets/c2dcfe37-c622-41f0-894f-0714424829e7)

## 📖 Description
아보월드는 아보카도 정보 공유를 위한 커뮤니티입니다.<br />
* [React 프론트엔드 레포지토리](https://github.com/moolmin/avoworld-fe)
* [Spring 백엔드 레포지토리](https://github.com/moolmin/avoworld-be)

## 🕹️ Main Feature
### 랜딩 - 메인 페이지 
* 게시글 목록 조회

 ![랜딩](https://github.com/user-attachments/assets/48a52f16-8b2e-4567-ac14-617b6aede913) |![메인](https://github.com/user-attachments/assets/705ca46f-1abd-4c02-843b-8b9e0cc2bddc)
--- | --- | 


### 로그인 - 회원가입 페이지 
 ![Image 8-26-24 at 3 49 PM](https://github.com/user-attachments/assets/65466ee4-6ff4-4c60-8c85-71beb9df0f79) |![screencapture-d1jlocd3s0jxr6-cloudfront-net-register-2024-08-26-15_50_04](https://github.com/user-attachments/assets/7e349d7f-bad1-4cd7-8be6-deedb3f6f6d5)
--- | --- | 


### 상세 페이지
* 게시글 상세 조회
* 댓글 작성, 수정, 삭제
![상세페이지](https://github.com/user-attachments/assets/33c1e844-ac63-40ac-ba3c-8867f15161e0)

## 🔧 Stack
* **Frontend**: `React` `JavaScript`
* **Backend**: `Springboot`
* **Database** : `AWS RDS (MySQL)`
* **DevOps**: `AWS EC2`

## 🌐 Architecture
![image](https://github.com/user-attachments/assets/f92563a5-a420-4bcc-93bb-488ca6985524)
* `Github actions`를 활용한 지속적 통합 및 배포
* **BE**: `deploy`브랜치에서 push 발생시 Spring boot 애플리케이션을 Dockerfile을 사용해 `Docker image`빌드
* **FE**: `deploy`브랜치에서 push 발생시 React 애플리케이션을 `AWS S3`버킷에 배포



