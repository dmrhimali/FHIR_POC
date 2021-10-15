# KafkaToFhir

Ingestion framework

Need fhirserver(pain or jpa-server) running for this to work

## LOCAL KAFKA DEBEZIUM SETUP  (Recommended)

Here we will locally install kafka and use debezium to bring docker postgres table: public.member to kafka topic: dbserver1.public.member

### step1: run postgres docker container with member table:

```sh
docker-compose up -d postgres
```

connect to your db using datagrip (host: localhost, usr=postgres, pass=postgres, db=postgres) and you should see populated member table:
```sh
| id | status | first\_name | last\_name | date\_of\_birth | gender | address\_line\_1 | address\_line\_2 | address\_line\_3 | city | state | zipcode | country | sponsor\_id |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | active | Greg | Jones | 1999-01-25 | male | 1 maple st | unit 1 | NULL | Decatur | Georgia | 10001 | Spain | 1 |
| 2 | active | Toby | Brown | 2011-01-09 | male | 2 wildflower st | suite 1 | NULL | Decatur | Georgia | 92067 | Spain | 2 |
| 3 | canceled | Jack | Brown | 1994-07-08 | female | 1 maple st | suite 1 | NULL | Plymouth | Minnesota | 92067 | India | 3 |
| 4 | canceled | Jill | Johnson | 2010-01-17 | male | 3 hunter ln | suite 1 | NULL | Atlanta | Georgia | 10001 | Spain | 3 |
| 5 | active | Tom | Johnson | 2008-01-29 | male | 3 hunter ln | suite 1 | NULL | Decatur | New  York | 92067 | Spain | 3 |
| 6 | active | Tim | Zack | 2008-01-29 | male | 3 hunter ln | suite 1 | NULL | Decatur | New  York | 92067 | Spain | 3 |

```

### step2: install kafka on local mac

prerequisite: need java installed

```sh
$ brew install kafka
```

This will install kafka on : `/usr/local/Cellar/kafka/`

The poroperies files are located at` /usr/local/Cellar/kafka/2.4.1/libexec/config`

### step3: run zookeeper
zookeeper will be installed with kafka. start it:

```sh
$ zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties
```

### step4: run kafka
Start Kafka server:

```sh
$ kafka-server-start /usr/local/etc/kafka/server.properties
```

list kafka topics:
```sh
$ kafka-topics --list --bootstrap-server localhost:9092
```

### step5: add debezium postgres kafka jars


Download  plugins archive:

```sh
$ wget https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.0.0.Final/debezium-connector-postgres-1.0.0.Final-plugin.tar.gz

# also avaiable for manual download here: https://repo1.maven.org/maven2/io/debezium/debezium-connector-postgres/1.7.0.Final/debezium-connector-postgres-1.7.0.Final-plugin.tar.gz
```

Unzip:

```sh
$ tar -xzf debezium-connector-postgres-1.7.0.Final-plugin.tar.gz
```

You will have following files :

```sh
debezium-connector-postgres-1.7.0.Final.jar
protobuf-java-3.8.0.jar
checker-qual-3.5.0.jar
postgresql-42.2.22.jar
failureaccess-1.0.1.jar
guava-30.0-jre.jar
debezium-api-1.7.0.Final.jar
debezium-core-1.7.0.Final.jar
```
Move them to /`usr/local/Cellar/kafka/3.0.0/libexec/libs`

stop and start kafka:

```sh
$ kafka-server-start /usr/local/etc/kafka/server.properties
```

### step6: run kafka connector for debezium

Create our own connector file `debezium_postgres_connect.properties` under `/usr/local/Cellar/kafka/3.0.0/libexec/config/`:

**debezium_postgres_connect.properties:**
```sh
name=genesis-connector
connector.class=io.debezium.connector.postgresql.PostgresConnector
tasks.max=1
database.hostname=localhost
database.port=25432
database.user=postgres
database.password=postgres
database.dbname=postgres
database.server.name=dbserver1
database.history.kafka.bootstrap.servers=kafka:9092
database.history.kafka.topic=schema-changes.genesis
table.whitelist=public.member
errors.log.enable=true
```

### step7: Run kafka connect with our custom debezium config:

You may need to restart kafka/zookeeper:

```sh
# stop kafka, zookeeper (CTRL+C)

$ rm -rf /usr/local/var/lib/zookeeper/*

$ rm -rf /usr/local/var/lib/kafka-logs/*

$ zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties

$ kafka-server-start /usr/local/etc/kafka/server.properties
```

Run connector. This is what brings in cdc data from member table to kafka topic dbserver1.public.member

```sh
$ cd /usr/local/Cellar/kafka/3.0.0/libexec/config

$ connect-standalone connect-standalone.properties debezium_postgres_connect.properties
```


### step8: verify data sent:

run topic console consumer:
```sh
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic dbserver1.public.member --from-beginning
```

make some changes to table record, and it should show in console.

output:
```sh
```sh
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":935,"status":"active","first_name":"Tom","last_name":"Smith","date_of_birth":16910,"gender":"f","address_line_1":"2 wildflower st","address_line_2":"suite 1","address_line_3":null,"city":"Decatur","state":"Minnesota","zipcode":"92067","country":"United States","sponsor_id":3},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":936,"status":"active","first_name":"Silvester","last_name":"Jones","date_of_birth":12730,"gender":"m","address_line_1":"1 maple st","address_line_2":"suite 1","address_line_3":null,"city":"Plymouth","state":"New  York","zipcode":"92067","country":"Spain","sponsor_id":2},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":937,"status":"canceled","first_name":"Silvester","last_name":"Smith","date_of_birth":13384,"gender":"f","address_line_1":"2 wildflower st","address_line_2":"suite 1","address_line_3":null,"city":"Plymouth","state":"New  York","zipcode":"90209","country":"Spain","sponsor_id":1},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":938,"status":"active","first_name":"Tom","last_name":"Smith","date_of_birth":10709,"gender":"m","address_line_1":"1 maple st","address_line_2":"suite 1","address_line_3":null,"city":"Atlanta","state":"Minnesota","zipcode":"10001","country":"United States","sponsor_id":3},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":939,"status":"active","first_name":"Jill","last_name":"Milller","date_of_birth":13390,"gender":"f","address_line_1":"2 wildflower st","address_line_2":"suite 1","address_line_3":null,"city":"Decatur","state":"New  York","zipcode":"10001","country":"Spain","sponsor_id":3},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
{"schema":{"type":"struct","fields":[{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"before"},{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"status"},{"type":"string","optional":true,"field":"first_name"},{"type":"string","optional":true,"field":"last_name"},{"type":"int32","optional":true,"name":"io.debezium.time.Date","version":1,"field":"date_of_birth"},{"type":"string","optional":true,"field":"gender"},{"type":"string","optional":true,"field":"address_line_1"},{"type":"string","optional":true,"field":"address_line_2"},{"type":"string","optional":true,"field":"address_line_3"},{"type":"string","optional":true,"field":"city"},{"type":"string","optional":true,"field":"state"},{"type":"string","optional":true,"field":"zipcode"},{"type":"string","optional":true,"field":"country"},{"type":"int64","optional":true,"field":"sponsor_id"}],"optional":true,"name":"dbserver1.public.member.Value","field":"after"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":true,"field":"sequence"},{"type":"string","optional":false,"field":"schema"},{"type":"string","optional":false,"field":"table"},{"type":"int64","optional":true,"field":"txId"},{"type":"int64","optional":true,"field":"lsn"},{"type":"int64","optional":true,"field":"xmin"}],"optional":false,"name":"io.debezium.connector.postgresql.Source","field":"source"},{"type":"string","optional":false,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"},{"type":"int64","optional":false,"field":"total_order"},{"type":"int64","optional":false,"field":"data_collection_order"}],"optional":true,"field":"transaction"}],"optional":false,"name":"dbserver1.public.member.Envelope"},"payload":{"before":null,"after":{"id":940,"status":"canceled","first_name":"Silvester","last_name":"Jones","date_of_birth":12469,"gender":"m","address_line_1":"1 maple st","address_line_2":"unit 1","address_line_3":null,"city":"Atlanta","state":"Georgia","zipcode":"90209","country":"United States","sponsor_id":1},"source":{"version":"1.7.0.Final","connector":"postgresql","name":"dbserver1","ts_ms":1634309812863,"snapshot":"true","db":"postgres","sequence":"[null,\"34442832\"]","schema":"public","table":"member","txId":615,"lsn":34442832,"xmin":null},"op":"r","ts_ms":1634309812863,"transaction":null}}
```

### step9: Run KafkaToFhirIngestionApplication.

You would see messages coming through. in spring boot run console. Make changes to db records and observe messages.

I inserted folowing record to member table while app was runnig:

```sh
| id | status | first\_name | last\_name | date\_of\_birth | gender | address\_line\_1 | address\_line\_2 | address\_line\_3 | city | state | zipcode | country | sponsor\_id |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 9 | active | Jasmine | Page | 2008-01-29 | male | 3 hunter ln | suite 1 | NULL | Decatur | New  York | 92067 | Spain | 3 |

```

application output:

```sh
...

2021-10-15 12:20:43.635  INFO 63399 --- [pool-1-thread-1] o.a.kafka.common.utils.AppInfoParser     : Kafka version: 2.7.1
2021-10-15 12:20:43.635  INFO 63399 --- [pool-1-thread-1] o.a.kafka.common.utils.AppInfoParser     : Kafka commitId: 61dbce85d0d41457
2021-10-15 12:20:43.635  INFO 63399 --- [pool-1-thread-1] o.a.kafka.common.utils.AppInfoParser     : Kafka startTimeMs: 1634318443635
2021-10-15 12:20:43.637  INFO 63399 --- [pool-1-thread-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-input-group-1-4, groupId=input-group-1] Cluster ID: bGrG2fchQe6rXjnRBTiTag
2021-10-15 12:20:43.646  INFO 63399 --- [pool-1-thread-1] o.a.k.c.c.internals.AbstractCoordinator  : [Consumer clientId=consumer-input-group-1-4, groupId=input-group-1] Discovered group coordinator localhost:9092 (id: 2147483647 rack: null)
2021-10-15 12:21:36.143  INFO 63399 --- [container-0-C-1] ication$$EnhancerBySpringCGLIB$$c481ff26 : received a message  of type c for member id: 7 
2021-10-15 12:21:40.074  INFO 63399 --- [container-0-C-1] ication$$EnhancerBySpringCGLIB$$c481ff26 : output create : http://localhost:8080/fhir/Patient/53/_history/1 
2021-10-15 12:21:40.075  INFO 63399 --- [container-0-C-1] ication$$EnhancerBySpringCGLIB$$c481ff26 : processing complete.

```

Note the fhir patient id from above url: `http://localhost:8080/fhir/Patient/53/_history/1`


Now check if it was created in fhir backend: **GET http://localhost:8080/fhir/Patient/53/**

output:

```json
{
    "resourceType": "Patient",
    "id": "56",
    "meta": {
        "versionId": "2",
        "lastUpdated": "2021-10-15T19:11:17.973+00:00",
        "source": "#17Uh6xUpIYgG4kMb"
    },
    "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Jasmine </div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>9</td></tr><tr><td>Address</td><td><span>3 hunter ln </span><br/></td></tr><tr><td>Date of birth</td><td><span>31 December 1969</span></td></tr></tbody></table></div>"
    },
    "identifier": [
        {
            "system": "VP",
            "value": "9"
        }
    ],
    "active": false,
    "name": [
        {
            "given": [
                "Jasmine"
            ]
        },
        {
            "family": "Page"
        }
    ],
    "gender": "female",
    "birthDate": "1969-12-31",
    "address": [
        {
            "line": [
                "3 hunter ln"
            ]
        },
        {
            "line": [
                "suite 1"
            ]
        },
        {
            "city": "Decatur"
        },
        {
            "state": "New  York"
        },
        {
            "postalCode": "92067"
        }
    ]
}
```

Note fhir patient resource id is **56** because fhir generate a patient_uuid.


Now let's update that db record ast name to "Brown":
```sh
| id | status | first\_name | last\_name | date\_of\_birth | gender | address\_line\_1 | address\_line\_2 | address\_line\_3 | city | state | zipcode | country | sponsor\_id |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 9 | canceled | Jasmine | Brown | 2008-01-29 | female | 3 hunter ln | suite 1 | NULL | Decatur | New  York | 92067 | Spain | 3 |
```

app output:
```sh
2021-10-15 14:15:51.677  INFO 68288 --- [container-0-C-1] lication$$EnhancerBySpringCGLIB$$61387d4 : received a message  of type u for member id: 9 
2021-10-15 14:15:51.827  INFO 68288 --- [container-0-C-1] lication$$EnhancerBySpringCGLIB$$61387d4 : output update : http://localhost:8080/fhir/Patient/56/_history/3 
2021-10-15 14:15:51.827  INFO 68288 --- [container-0-C-1] lication$$EnhancerBySpringCGLIB$$61387d4 : processing complete.
```

It is reflected in fhir api:

**GET http://localhost:8080/fhir/Patient/56**:

ouptput:
```sh
{
    "resourceType": "Patient",
    "id": "56",
    "meta": {
        "versionId": "2",
        "lastUpdated": "2021-10-15T19:11:17.973+00:00",
        "source": "#17Uh6xUpIYgG4kMb"
    },
    "text": {
        "status": "generated",
        "div": "<div xmlns=\"http://www.w3.org/1999/xhtml\"><div class=\"hapiHeaderText\">Jasmine </div><table class=\"hapiPropertyTable\"><tbody><tr><td>Identifier</td><td>9</td></tr><tr><td>Address</td><td><span>3 hunter ln </span><br/></td></tr><tr><td>Date of birth</td><td><span>31 December 1969</span></td></tr></tbody></table></div>"
    },
    "identifier": [
        {
            "system": "VP",
            "value": "9"
        }
    ],
    "active": false,
    "name": [
        {
            "given": [
                "Jasmine"
            ]
        },
        {
            "family": "Page"
        }
    ],
    "gender": "female",
    "birthDate": "1969-12-31",
    "address": [
        {
            "line": [
                "3 hunter ln"
            ]
        },
        {
            "line": [
                "suite 1"
            ]
        },
        {
            "city": "Decatur"
        },
        {
            "state": "New  York"
        },
        {
            "postalCode": "92067"
        }
    ]
}
```

same goes for update and delete.


## DOCKER KAFKA DEBEZIUM CDC SETUP  (connection to bootstrap server from spring boot didnt work)

**Note**
- accessing kafka bootrstap server using springboot (application.yml) did not workout
- cdcing data from a postgres table to kafka topic worked out
## Set up cdc from postgres member table to kafka using debezium

1. Start a PostgreSQL instance:
2. Create database, member table
3. Start a Zookeeper instance:
4. Start a Kafka instance:
5. Start a Debezium kafka connect instance:
6. deploy postgres connector
7. View load event
8. View update event
9. View insert event
10. View delete event

## 1.Start a PostgreSQL instance:
    docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres debezium/example-postgres:1.0


## 2. Create database, member table
```shelldoc
docker exec -it postgres psql -U postgres

postgres # CREATE DATABASE genesis;

postgres # \c genesis

genesis #

CREATE TABLE public.member (
id bigint NOT NULL,
first_name character varying(100),
last_name character varying(100),
display_name character varying(100),
profile_picture character varying,
date_of_birth date,
email_address character varying(255) NOT NULL,
is_deleted boolean DEFAULT false NOT NULL,
vhm_legacy_id bigint NOT NULL,
created_date timestamp with time zone DEFAULT now() NOT NULL,
updated_date timestamp with time zone,
gender character(1),
background_image character varying(255),
words_to_live_by character varying(255),
my_bio text,
sponsor_id bigint,
timezone_id bigint,
language character varying(15),
unit_of_measurement character varying(50),
business_unit_id bigint,
office_id bigint,
is_wizard_form_completed boolean DEFAULT false,
is_internal boolean DEFAULT false,
member_settings_id bigint NOT NULL,
status character varying(50) DEFAULT 'Active'::character varying NOT NULL,
title character varying(50),
department character varying(50),
location character varying(50),
type character varying(50) DEFAULT 'Primary'::character varying NOT NULL,
country character varying(3),
hire_date date,
original_hire_date date,
employee_id character varying(250),
division character varying(100),
failed_security_question_count integer,
security_question_lockout boolean,
country_id bigint,
user_name character varying,
phone_number character varying(30),
gender_identity character varying(255),
external_id character varying(32) DEFAULT md5(((random())::text || (clock_timestamp())::text)),
state_id bigint,
enrollment_date timestamp with time zone
);

genesis #
ALTER TABLE ONLY public.member ADD CONSTRAINT pk_member PRIMARY KEY (id);

genesis #
INSERT INTO public.member (id, first_name, last_name, display_name, profile_picture, date_of_birth, email_address, is_deleted, vhm_legacy_id, created_date, updated_date, gender, background_image, words_to_live_by, my_bio, sponsor_id, timezone_id, language, unit_of_measurement, business_unit_id, office_id, is_wizard_form_completed, is_internal, member_settings_id, status, title, department, location, type, country, hire_date, original_hire_date, employee_id, division, failed_security_question_count, security_question_lockout, country_id, user_name, phone_number, gender_identity, external_id, state_id, enrollment_date)
VALUES (141418, 'Jasenko', 'Hadziomeragic', 'Jasenko Hadziomeragic', 'https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif', '1988-04-05', 'test111@live.com', false, 133227, '2014-12-16 15:39:24.866+00', '2019-12-13 16:12:52.555+00', 'M', NULL, NULL, NULL, 38383, 35, 'en_us', 'Imperial', NULL, NULL, NULL, false, 8606, 'Cancelled', NULL, NULL, NULL, 'Primary', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Male', '46d790dc7e7283c169e5fa201d7e39ee', NULL, '2014-12-16 15:39:24.866+00');
```

## 3. Start a Zookeeper instance:
docker run -it --rm --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 debezium/zookeeper:1.0


## 4. Start a Kafka instance:

docker run -it --rm --name kafka -p 9092:9092 -p 29092:29092 \
-e KAFKA_LISTENERS=PLAINTEXT://:29092,EXTERNAL://:9092 \
-e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:29092,EXTERNAL://localhost:9092 \
-e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT \
-e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
-e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true \
--link zookeeper:zookeeper debezium/kafka:1.0



## 5. Start a Debezium kafka connect instance:
docker run -it --rm --name connect -p 8083:8083 -e GROUP_ID=1 -e CONFIG_STORAGE_TOPIC=my-connect-configs -e OFFSET_STORAGE_TOPIC=my-connect-offsets -e ADVERTISED_HOST_NAME=$(echo $DOCKER_HOST | cut -f3 -d'/' | cut -f1 -d':') --link zookeeper:zookeeper --link postgres:postgres --link kafka:kafka debezium/connect:1.0


## 6. deploy postgres connector
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d ' { "name": "genesis-connector", "config": { "connector.class": "io.debezium.connector.postgresql.PostgresConnector", "tasks.max": "1", "database.hostname": "postgres", "database.port": "25432", "database.user": "postgres", "database.password": "postgres", "database.dbname" : "postgres", "database.server.name": "dbserver1", "database.whitelist": "postgres", "database.history.kafka.bootstrap.servers": "kafka:9092", "database.history.kafka.topic": "schema-changes.postgres" } }'

curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d ' { "name": "genesis-connector", "config": { "connector.class": "io.debezium.connector.postgresql.PostgresConnector", "tasks.max": "1", "database.hostname": "postgres", "database.port": "25432", "database.user": "postgres", "database.password": "postgres", "database.dbname" : "postgres", "database.server.name": "dbserver1", "database.whitelist": "postgres", "database.history.kafka.bootstrap.servers": "locahost:9092", "database.history.kafka.topic": "schema-changes.postgres" } }'


output:
```json
{
  "name":"genesis-connector",
  "config":
  {
    "connector.class":"io.debezium.connector.postgresql.PostgresConnector",
    "tasks.max":"1",
    "database.hostname":"postgres",
    "database.port":"25432",
    "database.user":"postgres",
    "database.password":"postgres",
    "database.dbname":"postgres",
    "database.server.name":"dbserver1",
    "database.whitelist":"genesis",
    "database.history.kafka.bootstrap.servers":"kafka:9092",
    "database.history.kafka.topic":"schema-changes.genesis",
    "name":"genesis-connector"
  },
  "tasks":[],
  "type":"source"
}%
```

Verify the Connector is created:

```shell
curl -X GET -H "Accept:application/json" localhost:8083/connectors/genesis-connector
```

output:
```json
{"name":"genesis-connector","config":{"connector.class":"io.debezium.connector.postgresql.PostgresConnector","database.user":"postgres","database.dbname":"genesis","tasks.max":"1","database.hostname":"postgres","database.password":"postgres","database.history.kafka.bootstrap.servers":"kafka:9092","database.history.kafka.topic":"schema-changes.genesis","name":"genesis-connector","database.server.name":"dbserver1","database.whitelist":"genesis","database.port":"5432"},"tasks":[{"connector":"genesis-connector","task":0}],"type":"source"}%

```

✘ rasanjalee.dissanaya@VP2461  /usr/local/etc  docker exec -ti kafka /kafka/bin/kafka-topics.sh --list --zookeeper zookeeper:2181
__consumer_offsets
connect-status
dbserver1.inventory.customers
dbserver1.inventory.geom
dbserver1.inventory.orders
dbserver1.inventory.products
dbserver1.inventory.products_on_hand
dbserver1.inventory.spatial_ref_sys
dbserver1.public.member
my-connect-configs
my-connect-offsets



rasanjalee.dissanaya@VP2461  /usr/local/etc  docker exec -ti kafka bash
[kafka@d4b0093742e9 ~]$ ls
LICENSE  NOTICE  bin  config  config.orig  data  libs  logs
[kafka@d4b0093742e9 ~]$ ls bin
connect-distributed.sh        kafka-configs.sh             kafka-delegation-tokens.sh  kafka-mirror-maker.sh                kafka-run-class.sh                  kafka-verifiable-consumer.sh     zookeeper-server-start.sh
connect-mirror-maker.sh       kafka-console-consumer.sh    kafka-delete-records.sh     kafka-preferred-replica-election.sh  kafka-server-start.sh               kafka-verifiable-producer.sh     zookeeper-server-stop.sh
connect-standalone.sh         kafka-console-producer.sh    kafka-dump-log.sh           kafka-producer-perf-test.sh          kafka-server-stop.sh                trogdor.sh                       zookeeper-shell.sh
kafka-acls.sh                 kafka-consumer-groups.sh     kafka-leader-election.sh    kafka-reassign-partitions.sh         kafka-streams-application-reset.sh  windows
kafka-broker-api-versions.sh  kafka-consumer-perf-test.sh  kafka-log-dirs.sh           kafka-replica-verification.sh        kafka-topics.sh                     zookeeper-security-migration.sh

✘ rasanjalee.dissanaya@VP2461  /usr/local/etc  docker exec -ti kafka /kafka/bin/kafka-topics.sh --create --topic test-topic --partitions 1 --replication-factor 1 --zookeeper zookeeper:2181
Created topic test-topic.


✘ rasanjalee.dissanaya@VP2461  /usr/local/etc  docker exec -ti zookeeper bin/zkCli.sh --server localhost:2181 get /brokers/ids/1
Connecting to localhost:2181
2021-10-07 01:59:08,323 - INFO  [main:Environment@109] - Client environment:zookeeper.version=3.5.6-c11b7e26bc554b8523dc929761dd28808913f091, built on 10/08/2019 20:18 GMT
2021-10-07 01:59:08,326 - INFO  [main:Environment@109] - Client environment:host.name=c245c4355af4
2021-10-07 01:59:08,326 - INFO  [main:Environment@109] - Client environment:java.version=11.0.6
2021-10-07 01:59:08,329 - INFO  [main:Environment@109] - Client environment:java.vendor=Oracle Corporation
2021-10-07 01:59:08,330 - INFO  [main:Environment@109] - Client environment:java.home=/usr/lib/jvm/java-11-openjdk-11.0.6.10-3.el7.x86_64
2021-10-07 01:59:08,330 - INFO  [main:Environment@109] - Client environment:java.class.path=/zookeeper/bin/../zookeeper-server/target/classes:/zookeeper/bin/../build/classes:/zookeeper/bin/../zookeeper-server/target/lib/*.jar:/zookeeper/bin/../build/lib/*.jar:/zookeeper/bin/../lib/zookeeper-jute-3.5.6.jar:/zookeeper/bin/../lib/zookeeper-3.5.6.jar:/zookeeper/bin/../lib/slf4j-log4j12-1.7.25.jar:/zookeeper/bin/../lib/slf4j-api-1.7.25.jar:/zookeeper/bin/../lib/netty-transport-native-unix-common-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-transport-native-epoll-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-transport-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-resolver-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-handler-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-common-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-codec-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-buffer-4.1.42.Final.jar:/zookeeper/bin/../lib/log4j-1.2.17.jar:/zookeeper/bin/../lib/json-simple-1.1.1.jar:/zookeeper/bin/../lib/jline-2.11.jar:/zookeeper/bin/../lib/jetty-util-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-servlet-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-server-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-security-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-io-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-http-9.4.17.v20190418.jar:/zookeeper/bin/../lib/javax.servlet-api-3.1.0.jar:/zookeeper/bin/../lib/jackson-databind-2.9.10.jar:/zookeeper/bin/../lib/jackson-core-2.9.10.jar:/zookeeper/bin/../lib/jackson-annotations-2.9.10.jar:/zookeeper/bin/../lib/commons-cli-1.2.jar:/zookeeper/bin/../lib/audience-annotations-0.5.0.jar:/zookeeper/bin/../zookeeper-*.jar:/zookeeper/bin/../zookeeper-server/src/main/resources/lib/*.jar:/zookeeper/bin/../conf:
2021-10-07 01:59:08,330 - INFO  [main:Environment@109] - Client environment:java.library.path=/usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
2021-10-07 01:59:08,330 - INFO  [main:Environment@109] - Client environment:java.io.tmpdir=/tmp
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:java.compiler=<NA>
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:os.name=Linux
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:os.arch=amd64
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:os.version=5.10.47-linuxkit
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:user.name=zookeeper
2021-10-07 01:59:08,331 - INFO  [main:Environment@109] - Client environment:user.home=/zookeeper
2021-10-07 01:59:08,332 - INFO  [main:Environment@109] - Client environment:user.dir=/zookeeper
2021-10-07 01:59:08,332 - INFO  [main:Environment@109] - Client environment:os.memory.free=25MB
2021-10-07 01:59:08,335 - INFO  [main:Environment@109] - Client environment:os.memory.max=256MB
2021-10-07 01:59:08,335 - INFO  [main:Environment@109] - Client environment:os.memory.total=32MB
2021-10-07 01:59:08,339 - INFO  [main:ZooKeeper@868] - Initiating client connection, connectString=localhost:2181 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@5afa04c
2021-10-07 01:59:08,347 - INFO  [main:X509Util@79] - Setting -D jdk.tls.rejectClientInitiatedRenegotiation=true to disable client-initiated TLS renegotiation
2021-10-07 01:59:08,359 - INFO  [main:ClientCnxnSocket@237] - jute.maxbuffer value is 4194304 Bytes
2021-10-07 01:59:08,372 - INFO  [main:ClientCnxn@1653] - zookeeper.request.timeout value is 0. feature enabled=
ZooKeeper -server host:port cmd args
addauth scheme auth
close
config [-c] [-w] [-s]
connect host:port
create [-s] [-e] [-c] [-t ttl] path [data] [acl]
delete [-v version] path
deleteall path
delquota [-n|-b] path
get [-s] [-w] path
getAcl [-s] path
history
listquota path
ls [-s] [-w] [-R] path
ls2 path [watch]
printwatches on|off
quit
reconfig [-s] [-v version] [[-file path] | [-members serverID=host:port1:port2;port3[,...]*]] | [-add serverId=host:port1:port2;port3[,...]]* [-remove serverId[,...]*]
redo cmdno
removewatches path [-c|-d|-a] [-l]
rmr path
set [-s] [-v version] path data
setAcl [-s] [-v version] [-R] path acl
setquota -n|-b val path
stat [-w] path
sync path
Command not found: Command not found localhost:2181
✘ rasanjalee.dissanaya@VP2461  /usr/local/etc  docker exec -ti zookeeper bin/zkCli.sh -server localhost:2181 get /brokers/ids/1
Connecting to localhost:2181
2021-10-07 02:00:41,595 - INFO  [main:Environment@109] - Client environment:zookeeper.version=3.5.6-c11b7e26bc554b8523dc929761dd28808913f091, built on 10/08/2019 20:18 GMT
2021-10-07 02:00:41,598 - INFO  [main:Environment@109] - Client environment:host.name=c245c4355af4
2021-10-07 02:00:41,598 - INFO  [main:Environment@109] - Client environment:java.version=11.0.6
2021-10-07 02:00:41,602 - INFO  [main:Environment@109] - Client environment:java.vendor=Oracle Corporation
2021-10-07 02:00:41,602 - INFO  [main:Environment@109] - Client environment:java.home=/usr/lib/jvm/java-11-openjdk-11.0.6.10-3.el7.x86_64
2021-10-07 02:00:41,603 - INFO  [main:Environment@109] - Client environment:java.class.path=/zookeeper/bin/../zookeeper-server/target/classes:/zookeeper/bin/../build/classes:/zookeeper/bin/../zookeeper-server/target/lib/*.jar:/zookeeper/bin/../build/lib/*.jar:/zookeeper/bin/../lib/zookeeper-jute-3.5.6.jar:/zookeeper/bin/../lib/zookeeper-3.5.6.jar:/zookeeper/bin/../lib/slf4j-log4j12-1.7.25.jar:/zookeeper/bin/../lib/slf4j-api-1.7.25.jar:/zookeeper/bin/../lib/netty-transport-native-unix-common-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-transport-native-epoll-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-transport-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-resolver-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-handler-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-common-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-codec-4.1.42.Final.jar:/zookeeper/bin/../lib/netty-buffer-4.1.42.Final.jar:/zookeeper/bin/../lib/log4j-1.2.17.jar:/zookeeper/bin/../lib/json-simple-1.1.1.jar:/zookeeper/bin/../lib/jline-2.11.jar:/zookeeper/bin/../lib/jetty-util-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-servlet-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-server-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-security-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-io-9.4.17.v20190418.jar:/zookeeper/bin/../lib/jetty-http-9.4.17.v20190418.jar:/zookeeper/bin/../lib/javax.servlet-api-3.1.0.jar:/zookeeper/bin/../lib/jackson-databind-2.9.10.jar:/zookeeper/bin/../lib/jackson-core-2.9.10.jar:/zookeeper/bin/../lib/jackson-annotations-2.9.10.jar:/zookeeper/bin/../lib/commons-cli-1.2.jar:/zookeeper/bin/../lib/audience-annotations-0.5.0.jar:/zookeeper/bin/../zookeeper-*.jar:/zookeeper/bin/../zookeeper-server/src/main/resources/lib/*.jar:/zookeeper/bin/../conf:
2021-10-07 02:00:41,604 - INFO  [main:Environment@109] - Client environment:java.library.path=/usr/java/packages/lib:/usr/lib64:/lib64:/lib:/usr/lib
2021-10-07 02:00:41,604 - INFO  [main:Environment@109] - Client environment:java.io.tmpdir=/tmp
2021-10-07 02:00:41,605 - INFO  [main:Environment@109] - Client environment:java.compiler=<NA>
2021-10-07 02:00:41,605 - INFO  [main:Environment@109] - Client environment:os.name=Linux
2021-10-07 02:00:41,605 - INFO  [main:Environment@109] - Client environment:os.arch=amd64
2021-10-07 02:00:41,606 - INFO  [main:Environment@109] - Client environment:os.version=5.10.47-linuxkit
2021-10-07 02:00:41,606 - INFO  [main:Environment@109] - Client environment:user.name=zookeeper
2021-10-07 02:00:41,606 - INFO  [main:Environment@109] - Client environment:user.home=/zookeeper
2021-10-07 02:00:41,606 - INFO  [main:Environment@109] - Client environment:user.dir=/zookeeper
2021-10-07 02:00:41,607 - INFO  [main:Environment@109] - Client environment:os.memory.free=25MB
2021-10-07 02:00:41,609 - INFO  [main:Environment@109] - Client environment:os.memory.max=256MB
2021-10-07 02:00:41,609 - INFO  [main:Environment@109] - Client environment:os.memory.total=32MB
2021-10-07 02:00:41,613 - INFO  [main:ZooKeeper@868] - Initiating client connection, connectString=localhost:2181 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@5afa04c
2021-10-07 02:00:41,619 - INFO  [main:X509Util@79] - Setting -D jdk.tls.rejectClientInitiatedRenegotiation=true to disable client-initiated TLS renegotiation
2021-10-07 02:00:41,633 - INFO  [main:ClientCnxnSocket@237] - jute.maxbuffer value is 4194304 Bytes
2021-10-07 02:00:41,643 - INFO  [main:ClientCnxn@1653] - zookeeper.request.timeout value is 0. feature enabled=
2021-10-07 02:00:41,672 - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@1112] - Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (unknown error)
2021-10-07 02:00:41,689 - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@959] - Socket connection established, initiating session, client: /127.0.0.1:55586, server: localhost/127.0.0.1:2181
2021-10-07 02:00:41,700 - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@1394] - Session establishment complete on server localhost/127.0.0.1:2181, sessionid = 0x100025ba5970005, negotiated timeout = 30000

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
{"listener_security_protocol_map":{"PLAINTEXT":"PLAINTEXT"},"endpoints":["PLAINTEXT://172.17.0.3:9092"],"jmx_port":-1,"host":"172.17.0.3","timestamp":"1633569184007","port":9092,"version":4}
rasanjalee.dissanaya@VP2461  /usr/local/etc 
## 7. start a Kafka Console consumer to watch changes:

```shell
docker run -it --rm --name watcher --link zookeeper:zookeeper --link kafka:kafka debezium/kafka:1.0 watch-topic -a -k dbserver1.public.member
```

## 8. View load event
Once watcher is run Y\you can now see load of (op: "r") of first load table records. each record has two json strings printed(one for key, one for value. payload field has id related to record being transferred. this contains before and after values ).



### LOAD

```json
//RECORD 1: key
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "int64",
        "optional": false,
        "field": "id"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Key"
  },
  "payload": {
    "id": 141418
  }
}

//RECORD 1: value

{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "before"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "after"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "string",
            "optional": false,
            "field": "version"
          },
          {
            "type": "string",
            "optional": false,
            "field": "connector"
          },
          {
            "type": "string",
            "optional": false,
            "field": "name"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "ts_ms"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.data.Enum",
            "version": 1,
            "parameters": {
              "allowed": "true,last,false"
            },
            "default": "false",
            "field": "snapshot"
          },
          {
            "type": "string",
            "optional": false,
            "field": "db"
          },
          {
            "type": "string",
            "optional": false,
            "field": "schema"
          },
          {
            "type": "string",
            "optional": false,
            "field": "table"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "txId"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "lsn"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "xmin"
          }
        ],
        "optional": false,
        "name": "io.debezium.connector.postgresql.Source",
        "field": "source"
      },
      {
        "type": "string",
        "optional": false,
        "field": "op"
      },
      {
        "type": "int64",
        "optional": true,
        "field": "ts_ms"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Envelope"
  },
  "payload": {
    "before": null,
    "after": {
      "id": 141418,
      "first_name": "Jasenko",
      "last_name": "Hadziomeragic",
      "display_name": "Jasenko Hadziomeragic",
      "profile_picture": "https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
      "date_of_birth": 6669,
      "email_address": "test111@live.com",
      "is_deleted": false,
      "vhm_legacy_id": 133227,
      "created_date": "2014-12-16T15:39:24.866Z",
      "updated_date": "2019-12-13T16:12:52.555Z",
      "gender": "M",
      "background_image": null,
      "words_to_live_by": null,
      "my_bio": null,
      "sponsor_id": 38383,
      "timezone_id": 35,
      "language": "en_us",
      "unit_of_measurement": "Imperial",
      "business_unit_id": null,
      "office_id": null,
      "is_wizard_form_completed": null,
      "is_internal": false,
      "member_settings_id": 8606,
      "status": "Cancelled",
      "title": null,
      "department": null,
      "location": null,
      "type": "Primary",
      "country": null,
      "hire_date": null,
      "original_hire_date": null,
      "employee_id": null,
      "division": null,
      "failed_security_question_count": null,
      "security_question_lockout": null,
      "country_id": null,
      "user_name": null,
      "phone_number": null,
      "gender_identity": "Male",
      "external_id": "46d790dc7e7283c169e5fa201d7e39ee",
      "state_id": null,
      "enrollment_date": "2014-12-16T15:39:24.866Z"
    },
    "source": {
      "version": "1.0.2.Final",
      "connector": "postgresql",
      "name": "dbserver1",
      "ts_ms": 1584551957919,
      "snapshot": "last",
      "db": "genesis",
      "schema": "public",
      "table": "member",
      "txId": 605,
      "lsn": 34198640,
      "xmin": null
    },
    "op": "r",
    "ts_ms": 1584551957926
  }
}
```

## 9. View update event

connect to database and execute following:


docker exec -it postgres psql -U postgres

\c genesis

UPDATE member SET first_name='Jasenk', last_name='Hadziomeragic M' WHERE id=141418


output from watcher (op: u for update):


```json
//RECORD 1: key
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "int64",
        "optional": false,
        "field": "id"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Key"
  },
  "payload": {
    "id": 141418
  }
}

//RECORD 1: value
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "before"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "after"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "string",
            "optional": false,
            "field": "version"
          },
          {
            "type": "string",
            "optional": false,
            "field": "connector"
          },
          {
            "type": "string",
            "optional": false,
            "field": "name"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "ts_ms"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.data.Enum",
            "version": 1,
            "parameters": {
              "allowed": "true,last,false"
            },
            "default": "false",
            "field": "snapshot"
          },
          {
            "type": "string",
            "optional": false,
            "field": "db"
          },
          {
            "type": "string",
            "optional": false,
            "field": "schema"
          },
          {
            "type": "string",
            "optional": false,
            "field": "table"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "txId"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "lsn"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "xmin"
          }
        ],
        "optional": false,
        "name": "io.debezium.connector.postgresql.Source",
        "field": "source"
      },
      {
        "type": "string",
        "optional": false,
        "field": "op"
      },
      {
        "type": "int64",
        "optional": true,
        "field": "ts_ms"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Envelope"
  },
  "payload": {
    "before": null,
    "after": {
      "id": 141418,
      "first_name": "Jasenk",
      "last_name": "Hadziomeragic M",
      "display_name": "Jasenko Hadziomeragic",
      "profile_picture": "https://mashauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
      "date_of_birth": 6669,
      "email_address": "test111@live.com",
      "is_deleted": false,
      "vhm_legacy_id": 133227,
      "created_date": "2014-12-16T15:39:24.866Z",
      "updated_date": "2019-12-13T16:12:52.555Z",
      "gender": "M",
      "background_image": null,
      "words_to_live_by": null,
      "my_bio": null,
      "sponsor_id": 38383,
      "timezone_id": 35,
      "language": "en_us",
      "unit_of_measurement": "Imperial",
      "business_unit_id": null,
      "office_id": null,
      "is_wizard_form_completed": null,
      "is_internal": false,
      "member_settings_id": 8606,
      "status": "Cancelled",
      "title": null,
      "department": null,
      "location": null,
      "type": "Primary",
      "country": null,
      "hire_date": null,
      "original_hire_date": null,
      "employee_id": null,
      "division": null,
      "failed_security_question_count": null,
      "security_question_lockout": null,
      "country_id": null,
      "user_name": null,
      "phone_number": null,
      "gender_identity": "Male",
      "external_id": "46d790dc7e7283c169e5fa201d7e39ee",
      "state_id": null,
      "enrollment_date": "2014-12-16T15:39:24.866Z"
    },
    "source": {
      "version": "1.0.2.Final",
      "connector": "postgresql",
      "name": "dbserver1",
      "ts_ms": 1584552981723,
      "snapshot": "false",
      "db": "genesis",
      "schema": "public",
      "table": "member",
      "txId": 606,
      "lsn": 34198960,
      "xmin": null
    },
    "op": "u",
    "ts_ms": 1584552981822
  }
}
```

## 10. View insert event

connect to database and execute following:

docker exec -it postgres psql -U postgres

\c genesis

INSERT INTO public.member (id, first_name, last_name, display_name, profile_picture, date_of_birth, email_address, is_deleted, vhm_legacy_id, created_date, updated_date, gender, background_image, words_to_live_by, my_bio, sponsor_id, timezone_id, language, unit_of_measurement, business_unit_id, office_id, is_wizard_form_completed, is_internal, member_settings_id, status, title, department, location, type, country, hire_date, original_hire_date, employee_id, division, failed_security_question_count, security_question_lockout, country_id, user_name, phone_number, gender_identity, external_id, state_id, enrollment_date)
VALUES (2265, 'Tony', 'Parker', 'Tony Parker', 'https://qaauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif', NULL, 'tonyPvP@gmail.com', false, 134295, '2014-05-07 12:53:43.799+00', '2014-05-07 12:53:43.799+00', 'M', NULL, NULL, NULL, NULL, 35, 'en_us', 'Imperial', NULL, NULL, true, false, 7928, 'Active', NULL, NULL, NULL, 'Primary', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Male', 'efd8cf4d5659733d42c54ba79db771be', NULL, '2014-05-07 12:53:43.799+00');


output from watcher (op: c for insert/create):

```json
//RECORD 2: key
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "int64",
        "optional": false,
        "field": "id"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Key"
  },
  "payload": {
    "id": 2265
  }
}'

//RECORD 2: value
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "before"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "after"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "string",
            "optional": false,
            "field": "version"
          },
          {
            "type": "string",
            "optional": false,
            "field": "connector"
          },
          {
            "type": "string",
            "optional": false,
            "field": "name"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "ts_ms"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.data.Enum",
            "version": 1,
            "parameters": {
              "allowed": "true,last,false"
            },
            "default": "false",
            "field": "snapshot"
          },
          {
            "type": "string",
            "optional": false,
            "field": "db"
          },
          {
            "type": "string",
            "optional": false,
            "field": "schema"
          },
          {
            "type": "string",
            "optional": false,
            "field": "table"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "txId"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "lsn"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "xmin"
          }
        ],
        "optional": false,
        "name": "io.debezium.connector.postgresql.Source",
        "field": "source"
      },
      {
        "type": "string",
        "optional": false,
        "field": "op"
      },
      {
        "type": "int64",
        "optional": true,
        "field": "ts_ms"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Envelope"
  },
  "payload": {
    "before": null,
    "after": {
      "id": 2265,
      "first_name": "Tony",
      "last_name": "Parker",
      "display_name": "Tony Parker",
      "profile_picture": "https://qaauth1.virginhealthmiles.com/images/ce64b453-f1a1-4f91-b450-20b7c11de5c0.gif",
      "date_of_birth": null,
      "email_address": "tonyPvP@gmail.com",
      "is_deleted": false,
      "vhm_legacy_id": 134295,
      "created_date": "2014-05-07T12:53:43.799Z",
      "updated_date": "2014-05-07T12:53:43.799Z",
      "gender": "M",
      "background_image": null,
      "words_to_live_by": null,
      "my_bio": null,
      "sponsor_id": null,
      "timezone_id": 35,
      "language": "en_us",
      "unit_of_measurement": "Imperial",
      "business_unit_id": null,
      "office_id": null,
      "is_wizard_form_completed": true,
      "is_internal": false,
      "member_settings_id": 7928,
      "status": "Active",
      "title": null,
      "department": null,
      "location": null,
      "type": "Primary",
      "country": null,
      "hire_date": null,
      "original_hire_date": null,
      "employee_id": null,
      "division": null,
      "failed_security_question_count": null,
      "security_question_lockout": null,
      "country_id": null,
      "user_name": null,
      "phone_number": null,
      "gender_identity": "Male",
      "external_id": "efd8cf4d5659733d42c54ba79db771be",
      "state_id": null,
      "enrollment_date": "2014-05-07T12:53:43.799Z"
    },
    "source": {
      "version": "1.0.2.Final",
      "connector": "postgresql",
      "name": "dbserver1",
      "ts_ms": 1584553306402,
      "snapshot": "false",
      "db": "genesis",
      "schema": "public",
      "table": "member",
      "txId": 607,
      "lsn": 34200376,
      "xmin": null
    },
    "op": "c",
    "ts_ms": 1584553306409
  }
}
```

## 11. View delete event
connect to database and execute following:

docker exec -it postgres psql -U postgres

\c genesis

DELETE FROM public.member WHERE id=2265;


output from watcher (op: d for delete):

```json
//RECORD 2: key
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "int64",
        "optional": false,
        "field": "id"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Key"
  },
  "payload": {
    "id": 2265
  }
}

//RECORD 2: value
{
  "schema": {
    "type": "struct",
    "fields": [
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "before"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "int64",
            "optional": false,
            "field": "id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "first_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "last_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "display_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "profile_picture"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "date_of_birth"
          },
          {
            "type": "string",
            "optional": false,
            "field": "email_address"
          },
          {
            "type": "boolean",
            "optional": false,
            "field": "is_deleted"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "vhm_legacy_id"
          },
          {
            "type": "string",
            "optional": false,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "created_date"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "updated_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender"
          },
          {
            "type": "string",
            "optional": true,
            "field": "background_image"
          },
          {
            "type": "string",
            "optional": true,
            "field": "words_to_live_by"
          },
          {
            "type": "string",
            "optional": true,
            "field": "my_bio"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "sponsor_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "timezone_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "language"
          },
          {
            "type": "string",
            "optional": true,
            "field": "unit_of_measurement"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "business_unit_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "office_id"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_wizard_form_completed"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "is_internal"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "member_settings_id"
          },
          {
            "type": "string",
            "optional": false,
            "field": "status"
          },
          {
            "type": "string",
            "optional": true,
            "field": "title"
          },
          {
            "type": "string",
            "optional": true,
            "field": "department"
          },
          {
            "type": "string",
            "optional": true,
            "field": "location"
          },
          {
            "type": "string",
            "optional": false,
            "field": "type"
          },
          {
            "type": "string",
            "optional": true,
            "field": "country"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "hire_date"
          },
          {
            "type": "int32",
            "optional": true,
            "name": "io.debezium.time.Date",
            "version": 1,
            "field": "original_hire_date"
          },
          {
            "type": "string",
            "optional": true,
            "field": "employee_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "division"
          },
          {
            "type": "int32",
            "optional": true,
            "field": "failed_security_question_count"
          },
          {
            "type": "boolean",
            "optional": true,
            "field": "security_question_lockout"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "country_id"
          },
          {
            "type": "string",
            "optional": true,
            "field": "user_name"
          },
          {
            "type": "string",
            "optional": true,
            "field": "phone_number"
          },
          {
            "type": "string",
            "optional": true,
            "field": "gender_identity"
          },
          {
            "type": "string",
            "optional": true,
            "field": "external_id"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "state_id"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.time.ZonedTimestamp",
            "version": 1,
            "field": "enrollment_date"
          }
        ],
        "optional": true,
        "name": "dbserver1.public.member.Value",
        "field": "after"
      },
      {
        "type": "struct",
        "fields": [
          {
            "type": "string",
            "optional": false,
            "field": "version"
          },
          {
            "type": "string",
            "optional": false,
            "field": "connector"
          },
          {
            "type": "string",
            "optional": false,
            "field": "name"
          },
          {
            "type": "int64",
            "optional": false,
            "field": "ts_ms"
          },
          {
            "type": "string",
            "optional": true,
            "name": "io.debezium.data.Enum",
            "version": 1,
            "parameters": {
              "allowed": "true,last,false"
            },
            "default": "false",
            "field": "snapshot"
          },
          {
            "type": "string",
            "optional": false,
            "field": "db"
          },
          {
            "type": "string",
            "optional": false,
            "field": "schema"
          },
          {
            "type": "string",
            "optional": false,
            "field": "table"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "txId"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "lsn"
          },
          {
            "type": "int64",
            "optional": true,
            "field": "xmin"
          }
        ],
        "optional": false,
        "name": "io.debezium.connector.postgresql.Source",
        "field": "source"
      },
      {
        "type": "string",
        "optional": false,
        "field": "op"
      },
      {
        "type": "int64",
        "optional": true,
        "field": "ts_ms"
      }
    ],
    "optional": false,
    "name": "dbserver1.public.member.Envelope"
  },
  "payload": {
    "before": {
      "id": 2265,
      "first_name": null,
      "last_name": null,
      "display_name": null,
      "profile_picture": null,
      "date_of_birth": null,
      "email_address": "",
      "is_deleted": false,
      "vhm_legacy_id": 0,
      "created_date": "1970-01-01T00:00:00Z",
      "updated_date": null,
      "gender": null,
      "background_image": null,
      "words_to_live_by": null,
      "my_bio": null,
      "sponsor_id": null,
      "timezone_id": null,
      "language": null,
      "unit_of_measurement": null,
      "business_unit_id": null,
      "office_id": null,
      "is_wizard_form_completed": null,
      "is_internal": null,
      "member_settings_id": 0,
      "status": "",
      "title": null,
      "department": null,
      "location": null,
      "type": "",
      "country": null,
      "hire_date": null,
      "original_hire_date": null,
      "employee_id": null,
      "division": null,
      "failed_security_question_count": null,
      "security_question_lockout": null,
      "country_id": null,
      "user_name": null,
      "phone_number": null,
      "gender_identity": null,
      "external_id": null,
      "state_id": null,
      "enrollment_date": null
    },
    "after": null,
    "source": {
      "version": "1.0.2.Final",
      "connector": "postgresql",
      "name": "dbserver1",
      "ts_ms": 1584553562165,
      "snapshot": "false",
      "db": "genesis",
      "schema": "public",
      "table": "member",
      "txId": 608,
      "lsn": 34202232,
      "xmin": null
    },
    "op": "d",
    "ts_ms": 1584553562168
  }
}
```

## List kafka topics

```shell
docker run -it --rm --link zookeeper:zookeeper debezium/kafka:1.0 list-topics
```

output:

Note that the **dbserver1.public.member** which was created automatically as a part of kafka cdc above is shown:

```shell
rasanjalee.dissanaya@VP2461  /usr/local/Cellar/kafka/3.0.0/libexec/libs/debezium_plugins  docker run -it --rm --link zookeeper:zookeeper debezium/kafka:1.0 list-topics
WARNING: Using default BROKER_ID=1, which is valid only for non-clustered installations.
Using ZOOKEEPER_CONNECT=172.17.0.3:2181
Using KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.17.0.7:9092
Listing topics...
__consumer_offsets
connect-status
dbserver1.public.member
my-connect-configs
my-connect-offsets
```

https://habr.com/en/post/529222/


STEP 6: IMPORT/EXPORT YOUR DATA AS STREAMS OF EVENTS WITH KAFKA CONNECT
You probably have lots of data in existing systems like relational databases or traditional messaging systems, along with many applications that already use these systems. Kafka Connect allows you to continuously ingest data from external systems into Kafka, and vice versa. It is thus very easy to integrate existing systems with Kafka. To make this process even easier, there are hundreds of such connectors readily available.

Take a look at the Kafka Connect section to learn more about how to continuously import/export your data into and out of Kafka.