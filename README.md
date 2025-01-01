# トークン認証バックエンド

このプロジェクトは、JWT (JSON Web Token) を使用した認証を実装するためのSpring Bootベースのバックエンドアプリケーションです。

## 主な機能

- **ユーザー登録**: 新規ユーザーをデータベースに登録します。
- **ユーザー認証**: ログイン情報を基にJWTトークンを発行します。
- **認証制御**: JWTトークンを使用して保護されたリソースにアクセスします。
- **ログアウト処理**: Redisを使用したブラックリスト方式でトークンを無効化します。

## 使用技術

- **Java**: バックエンドロジック
- **Spring Boot**: フレームワーク
- **JWT**: 認証と認可
- **Redis**: ブラックリスト方式によるトークン無効化
- **PostgreSQL**: データベース
- **Maven**: 依存関係管理とビルドツール

## 前提条件

- **Java Development Kit (JDK)**: バージョン11以上
- **Maven**: バージョン3.6以上
- **PostgreSQL**: 任意のバージョン
- **Redis Server**: 任意のバージョン

## インストールとセットアップ

### 1. リポジトリをクローン

```bash
git clone https://github.com/Hoppip1124/tokenauthentication-backend.git
cd tokenauthentication-backend
