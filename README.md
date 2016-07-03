a chat system
==============

どこぞで聞いたチャットシステムを真似て構築してみる

Usage
======

1. cassandraを起動する
    - 環境変数 CASSANDRA_HOST にcassandra のlisten hostを入れる。

#### Akka Cluster: seed0,seed1 nodeの起動

```
java -cp target/scala-2.11/chat-system-assembly-0.0.1-SNAPSHOT.jar -Dconfig.resource=/seed0.conf arimitsu.sf.a_chat_system.ChatSystem
```
```
java -cp target/scala-2.11/chat-system-assembly-0.0.1-SNAPSHOT.jar -Dconfig.resource=/seed1.conf arimitsu.sf.a_chat_system.ChatSystem
```

#### Akka Cluster: member nodeの起動
```
java -cp target/scala-2.11/chat-system-assembly-0.0.1-SNAPSHOT.jar -Dconfig.resource=/member.conf arimitsu.sf.a_chat_system.ChatSystem
```
環境変数AKKA_PORT=xxxxを変更し、別portをlistenするmember nodeを立ち上げることもできる

#### Spring Application の起動
```
java -cp target/scala-2.11/chat-system-assembly-0.0.1-SNAPSHOT.jar -Dconfig.resource=/application.conf arimitsu.sf.a_chat_system.SpringApp
```

http://localhost:8080/