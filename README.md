# Continuing
仲間を見つけて、一緒にZoomミーティングで習慣を継続させていくSNSアプリ。

**URL :** ~~https://initdomain.site/home~~

※現在、Zoom App Marketplace へのアプリ公開申請がまだ通っていないため、公開停止中です。

## 仕様/機能一覧
- **ユーザーアカウント機能**
    - ログイン、ログアウト
    - ユーザーアカウントの新規登録、一覧表示、詳細表示、削除
    - ユーザーアカウントの編集
        - メールアドレス、パスワード、プロフィールメッセージ変更
        - プロフィール画像変更
- **フォロー機能**
    - フォロー、フォロー解除
    - フォロー中/フォロワーの一覧表示
- **Zoomミーティング機能**
    
    ミーティングの新規作成、一覧表示、詳細表示、削除
    
- **ページネーション機能**
    - ミーティングの一覧表示
- **検索機能**
    - ミーティングの検索
    - ユーザーアカウントの検索
- **画像ファイルのアップロード機能 (AWS S3バケット)**
    - プロフィール画像のアップロード
- **フラッシュメッセージ表示機能**
    - ログイン、ログアウトの時
    - ユーザーの登録、削除、更新の時
    - ミーティングの作成、削除の時
    - 入力や操作エラー、検索して該当するミーティングやユーザーが見つからなかった時
- **継続の判定, 記録機能**
    
    23:59までに、ミーティングの開始か参加をすることができれば、継続日数が１日分増える
    
- **ランキング機能**
    
    1ヶ月毎にユーザーの継続日数でランキング表示をする
    
- **国際化対応(英語と日本語のみ)**
- **JUnitテスト(徐々に追加中)**


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

        IAM, VPC, EC2(ALB, Elastic IP), Route53, ACM, RDS(MySQL), S3, SNS

- **バージョン/ソースコード管理**
    - **Git, GitHub**
- その他使用ツール
  - Visual Studio Code(フロントエンド, README等)
  - SpringToolSuite4(バックエンド)
  - digrams.net(ER図, インフラ構成図)
  - Figma(ワイヤーフレーム, プロトタイプ, アプリアイコン)
  - Notion(テーブル定義等のドキュメント)

## インフラ(AWS)構成図
![インフラ(AWS)構成図](https://s3.us-west-2.amazonaws.com/secure.notion-static.com/019b7341-35b9-4d22-a379-5471dbcda834/%E3%82%A4%E3%83%B3%E3%83%95%E3%83%A9%28AWS%29%E6%A7%8B%E6%88%90%E5%9B%B3.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220224%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220224T033650Z&X-Amz-Expires=86400&X-Amz-Signature=77156948a94081e54707fa3fb1cd58b1f16b9f36bf94c9d98cd565802b67ebc1&X-Amz-SignedHeaders=host&response-content-disposition=filename%20%3D%22%25E3%2582%25A4%25E3%2583%25B3%25E3%2583%2595%25E3%2583%25A9%28AWS%29%25E6%25A7%258B%25E6%2588%2590%25E5%259B%25B3.png%22&x-id=GetObject)

## ER図
![ER図](https://s3.us-west-2.amazonaws.com/secure.notion-static.com/599208a8-ce4a-4055-ae06-684571175099/er.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20220224%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20220224T033749Z&X-Amz-Expires=86400&X-Amz-Signature=c197e2fa4ad133782754608018547d5c051e0c059cf86a6d2580de9373ee9f3a&X-Amz-SignedHeaders=host&response-content-disposition=filename%20%3D%22er.png%22&x-id=GetObject)
| テーブル名 | 説明 |
| --- | --- |
| users | ユーザーアカウント情報 |
| follows | ユーザー同士のフォロー関係情報 |
| meetings | ユーザーに作成されたミーティング情報 |
| joins | ユーザーのミーティング参加予約情報 |
| records | ユーザーが継続した習慣の記録情報 |
| deliveries | ユーザーのメール配信設定情報 |
| temporaries | 仮登録されたユーザーの一時保管場所 |


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


