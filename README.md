# Continuing
仲間を見つけて、一緒にZoomミーティングで習慣を継続させていくSNSアプリ。

**URL :** ~~https://initdomain.site/home~~

※現在、Zoom App Marketplace へのアプリ公開申請がまだ通っていないため、公開停止中です。

## アプリ使用時の画面
![アプリ使用時の画面](https://user-images.githubusercontent.com/62631497/156865794-ee88e5d9-31e8-429f-8361-5890cea14fa4.png)

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
![インフラ(AWS)構成図](https://user-images.githubusercontent.com/62631497/156865680-c4055543-1b8b-4ad6-ba38-81e81c73fbe8.png)

## ER図
![ER図](https://user-images.githubusercontent.com/62631497/156865717-db89473e-6bd2-4da9-bc2a-1ceaaab910c5.png)
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


