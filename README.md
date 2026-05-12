# 書籍管理システム

## 利用技術
- Language: Kotlin 2.2.21
- Framework: Spring Boot 4.0.6
- Database: PostgreSQL
- Persistence: jOOQ
- Migration: Flyway

## 要件
- 書籍と著者の情報をRDBに登録・更新できる機能
- 著者に紐づく本を取得できる機能

## 仮定
* 書籍登録時の著者は、登録済みの著者のみとする 
* 著者・書籍はUUIDで管理し、リクエスト時もそのIDを利用する
  * 登録済み著者のID検索のため、**課題範囲外だが検索APIを実装**
  * 動作確認をしやすくするため、すべての著者検索用途の空文字列検索を許容（実サービスなら不可）
* 既存著者と一致する情報を持った新規著者の登録は許容する 
  * 既存著者を名前で検索する機能を提供し、利用もとで判断する前提
* 既存書籍と一致する情報を持った新規書籍の登録は許容する
  * 実際ではISBN情報を追加して、重複を防げる前提 
* リクエスト時、日付のフォーマットは`YYYY-mm-dd`であることが担保されてる前提
  * 実サービスだと、フロントのUIなどで制御される前提
* リクエスト時、出版ステータスは`PUBLISHED|UNPUBLISHED|published|unpublished`のいずれである前提
  * 実サービスだと、フロントのUIなどで制御される前提
* ページネーションは利用元で設定することを前提
  * 今回は実装対象外と判断する 

## 考察
1. 著者:書籍はM:Nの関係を持ってる
- 著者と書籍のマッピング関係を持ったMappingテーブルを別度持たせる
- 新規登録時のクエリ効率化のため、jOOQのBatchを活用
- 取得時のクエリの効率化のため、Multisetを利用

2. バリデーション設計
- フロントでの制約がないため、すべてのエッジケースを考慮すると切りがない
- 課題の範囲として、ValidationはBean Validationで最低限だけ制御する
- 更新（PATCH）APIの場合、実サービスを考慮すると更新可能・不可のキーもあり得るため、DTOを完全に分ける

3. ドメイン中心での処理
- 最低限のバリデーション以外の例外は、すべてドメインに収める
- 更新時のデーター修正も、すべてドメインモデルに格納したうえで行う
  - これにより、Unit Testが完結にかける
  - 更新時の制約などがビジネスロジックとして把握しやすくなる
  - 実サービスだと、データーの肥大化によるメモリー効率なども考慮すべきだが今回は割愛する

4. 例外の課題
- 課題であるため、すべてドメイン層で発生させる
- 実サービスであれば、より簡略化・細分化する必要性もあるが、要件として定時されてないためある程度で収めた

5. Transactionの課題
- システムのRPSやインフラ予算などを考慮して、再設計の余地がある
  - Transaction内で行う「登録済み著者か否か」の判断を、Transaction開始前にする
  - 著者と書籍の関係性は非同期処理にする
- 上記内容は範囲外と判断し、必要である一連の処理を完結させるようにする

6. データーの構成問題
- 区別できるものがUUIDのみの設計であり、重複データー許容せざるを得ない
- 実サービスであれば、独自の著者IDや書籍のISBNなどを利用して重複を防げる
- タイトルや名前に関しても、「日本語名」「英語名」「原語名」などのデーター構成も可能

7. 値段はBigDecimalとして登録する
- RequestはLongで入力するが、ドメインではBigDecimalとして扱う
- 外貨・割引などの時により安定性が担保できる

8. Unit Testの対応範囲
- Controllerでは、最低限のUTとしてWebMvcTestを活用
- Domain/Serviceはビジネスロジックであり、すべてのUTを作成
- Repositoryそうは、jOOQを利用してるためUTは省略する
- DBとの連携はTestContainerなどが必要な時点で、UTではなくIntegration Testの領域のため省略する

## 実行方法
```bash
# DB起動
$ docker compose up -d

# Application起動
# 1. IntelliJから起動
# 2. コマンドラインで実行する場合
$ ./gradlew bootRun
```

## API一覧
| Method | Endpoint        | Description |
|--------|-----------------|-------------|
| POST   | /authors        | 新規著者登録      |
| PATCH  | /authors/{id}   | 著者情報更新      | 
| GET    | /authors/search | 著者検索（名前検索）  |
| GET	   | /authors/{id}   | 著者の書籍一覧     |
| POST   | /books          | 新規書籍登録      |
| PATCH  | /books/{id}     | 書籍情報更新      |

### 新規著者登録
#### Request
| Method | Endpoint | Query | Content-Type     |
|--------|----------|-------|------------------|
| POST   | /authors | (なし)　 | application/json |

#### Request Body
| key        | value  | 
|------------|--------|
| name       | 著者の氏名  |
| birthDate  | 著者の誕生日 |

#### Sample
```
curl -X POST http://localhost:8080/authors \
     -H "Content-Type: application/json" \
     -d '{
       "name": "Test Author",
       "birthDate": "1990-01-01"
     }'
```
```
{
  "id":"00000000-0000-0000-0000-000000000001",
  "name":"Test Author",
  "birthDate":"1990-01-01"
}
```

#### Response
| HTTP Status | Response Body/Error Message | Description |
|-------------|-----------------------------|-------------|
| 201         | （省略）                        | 登録完了した著者情報  | 
| 500         | Failed to register author.  | 登録失敗        |

### 著者情報更新
#### Request
| Method | Endpoint      | Query | Content-Type     |
|--------|---------------|-------|------------------|
| PATCH  | /authors/{id} | (なし)　 | application/json |

#### Request Body

| key       | value                | 
|-----------|----------------------|
| name      | 著者の氏名。含まれないと更新されない。  |
| birthDate | 著者の誕生日。含まれないと更新されない。 |

#### Sample
```
curl -X PATCH http://localhost:8080/authors/00000000-0000-0000-0000-000000000001 \
     -H "Content-Type: application/json" \
     -d '{
       "birthDate": "1980-01-01"
     }'
```
```
{
  "id":"00000000-0000-0000-0000-000000000001",
  "name":"Test Author",
  "birthDate":"1980-01-01"
}
```
#### Response

| HTTP Status | Message                       | Description    |
|-------------|-------------------------------|----------------|
| 200         | （省略）                        | 更新完了した著者情報     |
| 404         | Failed to find target author. | 該当IDの著者が見つからない |
| 500         | Failed to register author.    | 登録失敗           |

### 著者検索（名前検索）
#### Request
| Method | Endpoint        | Query | Content-Type     |
|--------|-----------------|-------|------------------|
| GET    | /authors/search | name　 | application/json |

#### Request Body
なし

#### Sample
```
curl -X GET http://localhost:8080/authors/search?name=Author \
```
```
[
  {
    "id":"00000000-0000-0000-0000-000000000001",
    "name":"Test Author",
    "birthDate":"1980-01-01"
  }
]
```
#### Response

| HTTP Status | Message                          | Description |
|-------------|----------------------------------|-------------|
| 200         | （省略）                             | 検索結果の著者リスト  |
| 500         | Failed to search author by name. | 検索失敗        |

### 著者の書籍一覧
#### Request Path
| Method | Endpoint      | Query | Content-Type     |
|--------|---------------|-------|------------------|
| GET    | /authors/{id} | (なし)　 | application/json |


#### Request Body
なし

#### Sample
```
curl -X GET http://localhost:8080/authors/00000000-0000-0000-0000-000000000001 \
```
```
{
  "id":"00000000-0000-0000-0000-000000000001",
  "name":"Test Author",
  "birthDate":"1990-01-01",
  "books": [
    {
      "id": "00000000-0000-0000-0001-000000000001"
      "title": "Test Book 1"
    },
    {
      "id": "00000000-0000-0000-0001-000000000002"
      "title": "Test Book 2"
    }
  ]
}
```
#### Response

| HTTP Status | Message                | Description  |
|-------------|------------------------|--------------|
| 200         | （省略）                   | 著者に紐づいた書籍リスト |
| 500         | Failed to find author. | 対象となる著者取得失敗  |
| 400         | Author not found.      | 登録してない著者     |
| 500         | Failed to find book.   | 書籍所得失敗       |


### 新規書籍登録
#### Request
| Method | Endpoint | Query | Content-Type     |
|--------|----------|-------|------------------|
| POST   | /books   | (なし)　 | application/json |

#### Request Body
| key       | value      | 
|-----------|------------|
| title     | 書籍のタイトル    |
| price     | 書籍の価格      |
| authorIds | 書籍の著者のID配列 |
| status    | 書籍の出版状況    |

#### Sample
```
curl -X POST http://localhost:8080/books \                                                    
     -H "Content-Type: application/json" \
     -d '{                      
       "title": "Test Book 1",
       "price": 1000,
       "authorIds": ["00000000-0000-0000-0000-000000000001"],
       "status": "PUBLISHED"
     }'
```
```
｛
  "id":"00000000-0000-0000-0001-000000000001",
  "title":"Test Book 1",
  "price":1000,
  "authors":[
    {
      "id":"00000000-0000-0000-0000-000000000001",
      "name":"TestAuthor 1"
    }
  ],
  "status":"PUBLISHED"
}
```

#### Response
| HTTP Status | Message                                     | Description   |
|-------------|---------------------------------------------|---------------|
| 201         | （省略）                                        | 登録完了した書籍情報    |
| 400         | Author list contains not registered author. | 未登録著者IDが含まれてる |
| 500         | Failed to register book.                    | 書籍登録失敗        |


### 書籍情報更新
#### Request
| Method | Endpoint    | Query | Content-Type     |
|--------|-------------|-------|------------------|
| PATCH  | /books/{id} | (なし)　 | application/json |

#### Request Body
| key       | value      | 
|-----------|------------|
| title     | 書籍のタイトル    |
| price     | 書籍の価格      |
| authorIds | 書籍の著者のID配列 |
| status    | 書籍の出版状況    |

#### Sample
```
curl -X PATCH http://localhost:8080/books/00000000-0000-0000-0001-000000000001 \                                                    
     -H "Content-Type: application/json" \
     -d '{                      
       "price": 10000
     }'
```
```
{
  "id":"00000000-0000-0000-0001-000000000001",
  "title":"Test Book 1",
  "price":10000,
  "authors":[
    {
      "id":"00000000-0000-0000-0000-000000000001",
      "name":"TestAuthor 1"
    }
  ],
  "status":"PUBLISHED"
```

#### Error Response
| HTTP Status | Message                                             | Description                |
|-------------|-----------------------------------------------------|----------------------------|
| 200         | （省略）                                                | 更新完了した書籍情報                 |
| 404         | Book not found.                                     | 未登録書籍ID                    |
| 400         | Author list contains not registered author.         | 未登録著者IDが含まれてる              |
| 400         | PublicationStatus cannot be updated to unpublished. | PUBLISHEDからUNPUBLISHEDへの更新 |
| 500         | Failed to update book.                              | 書籍情報更新失敗                   |

