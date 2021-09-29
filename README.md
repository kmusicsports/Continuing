# Continuing
仲間を見つけて、一緒にZoomミーティングで習慣を継続させていくSNSアプリ。

**URL :** 


## 仕様/機能一覧
- **レスポンシブWebデザイン**
- 

## 使用技術(ツール)一覧
- **アプリケーション**
    - フロントエンド
        - **Bootstrap**
    - バックエンド
        - **Java**
        - **Maven**
        - **Spring Boot**
        - **Zoom API**
- **インフラ**
    - **AWS**

        IAM, VPC, EC2(ALB, Auto Scaling), Route53, ACM, RDS, S3, SNS
    - **MySQL**
- **バージョン/ソースコード管理**
    - **Git, GitHub**
- その他使用ツール
  - Visual Studio Code(フロントエンド等)
  - SpringToolSuite4(バックエンド)
  - digrams.net(ER図, インフラ構成図)
  - Figma (ワイヤーフレーム, プロトタイプ)


## インフラ(AWS)構成図
![インフラ(AWS)構成図]()


## テーブル定義


## ER図
![ER図]()


## ライセンス
このソフトウェアは、
[Apache 2.0ライセンス](http://www.apache.org/licenses/LICENSE-2.0)
で配布されている製作物
 [sgysgs/Spring_Zoom](https://github.com/sgysgs/Spring_Zoom)
が含まれています。
使用及び改変を行っている箇所は、以下の通りです。(配布元->Continuing)
- Spring_Zoom/src/main/java/com/min/study/zoom/ZoomApiIntegration.java
-> Continuing/src/main/java/com/example/continuing/zoom/ZoomApiIntegration.java
- Spring_Zoom/src/main/java/com/min/study/zoom/ZoomDetails.java
-> Continuing/src/main/java/com/example/continuing/zoom/ZoomDetails.java
- Spring_Zoom/src/main/java/com/min/study/zoom/controller/CreateMeetingController.java
-> Continuing/src/main/java/com/example/continuing/controller/meeting/CreateMeetingController.java
- Spring_Zoom/src/main/java/com/min/study/zoom/controller/DeleteMeetingController.java
-> Continuing/src/main/java/com/example/continuing/controller/meeting/DeleteMeetingController.java
- Spring_Zoom/src/main/java/com/min/study/zoom/dto/meetingDTO.java
-> Continuing/src/main/java/com/example/continuing/dto/MeetingDto.java


