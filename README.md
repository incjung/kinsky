Kinsky: Clojure Mapr Streams/Kafka client library
====================================
This is from [pyr/kinsky](http://https://github.com/pyr/kinsky)

[![Build Status](https://secure.travis-ci.org/pyr/kinsky.png)](http://travis-ci.org/pyr/kinsky)

Kinsky is a *somewhat* opinionated client library
for [Mapr Stream Streams](http://maprdocs.mapr.com/51/MapR_Streams/mapr_streams.html) in Clojure.

Kinsky provides the following:

- MapR Streams/Kakfa 0.9.0.x compatibility
- Adequate data representation of Kafka types.
- Default serializer and deserializer implementations such as
  **JSON**, **EDN** and a **keyword** serializer for keys.
- A `core.async` facade for producers and consumers.
- Documentation

## Usage

```clojure
[org.clojars.incjung/kinsky "0.1.15"]
```

## Documentation

* [API Documentation](http://pyr.github.io/kinsky)

## MapR Streams
MapR Streams brings integrated publish/subscribe messaging to the MapR Converged Data Platform.

Producer applications can publish messages to topics, which are logical collections of messages, that are managed by MapR Streams. Consumer applications can then read those messages at their own pace. All messages published to MapR Streams are persisted, allowing future consumers to “catch-up” on processing, and analytics applications to process historical data.

* [MapR Stream Documentation](http://maprdocs.mapr.com/51/MapR_Streams/getting_started_with_mapr_streams.html)

### Create a Stream 
```bash 
maprcli stream create -path /sample-stream
maprcli stream edit -path /sample-stream -produceperm p -consumeperm p -topicperm p
maprcli stream topic create -path /sample-stream -topic events
```
The two additional parameters grant security permissions. By default, these permissions are granted to the user ID that ran the maprcli stream create command.
-consumeperm
Grants permission to read messages from topics that are in the stream.
-produceperm
Grants permission to publish messages to topics that are in the stream.

### Create a topic 
```bash 
maprcli stream topic create -path /sample-stream -topic events
```


## Examples

The examples assume the following require forms:

```clojure
(:require [kinsky.client      :as client]
          [kinsky.async       :as async]
          [clojure.core.async :refer [go <! >!]])
```

MapR Streams
;; topic = "sample-stream:fast-messages"

### Production

```clojure
(let [p (client/producer {} :string :string)]
    (client/send! p "/sample-stream:events" "IJUNG" "HELLO WORLD"))


(let [p (client/producer {} :keyword :edn)]
    (client/send! p "/sample-stream:events" :hello {:hello :world}))
```
Async facade:

```clojure
(let [[in out] (async/producer {:bootstrap.servers "localhost:9092"} :keyword :edn)]
   (go
     (>! in {:topic "account" :key :account-a :value {:action :login}})
     (>! in {:topic "account" :key :account-a :value {:action :logout}})))
```

### Consumption

```clojure
(defn receiving [] 
  (let [c (client/consumer {:group.id "mygroup"} :keyword :edn)]
    (client/subscribe! c ["/sample-stream:events"])
    (while true
      (println (client/poll! c 1000)))))

(receiving)
```

Async facade:

```clojure
(let [[out ctl] (consumer {:bootstrap.servers "localhost:9092"
                           :group.id (str (java.util.UUID/randomUUID))}
                          (client/string-deserializer)
                          (client/string-deserializer))
      topic     "tests"]
						  
  (a/go-loop []
    (when-let [record (a/<! out)]
      (println (pr-str record))
      (recur)))
  (a/put! ctl {:op :partitions-for :topic topic})
  (a/put! ctl {:op :subscribe :topic topic})
  (a/put! ctl {:op :commit})
  (a/put! ctl {:op :pause :topic-partitions [{:topic topic :partition 0}
                                             {:topic topic :partition 1}
                                             {:topic topic :partition 2}
                                             {:topic topic :partition 3}]})
  (a/put! ctl {:op :resume :topic-partitions [{:topic topic :partition 0}
                                              {:topic topic :partition 1}
                                              {:topic topic :partition 2}
                                              {:topic topic :partition 3}]})
  (a/put! ctl {:op :stop}))
```

### Examples

#### Fusing two topics

```clojure
  (let [popts    {:bootstrap.servers "localhost:9092"}
        copts    (assoc popts :group.id "consumer-group-id")
        [in ctl] (kinsky.async/consumer copts :string :string)
        [out _]  (kinsky.async/producer popts :string :string)]

    (a/go
      ;; fuse topics
	  (a/>! ctl {:op :subscribe :topic "test1"})
      (let [transit (a/chan 10 (map #(assoc % :topic "test2")))]
        (a/pipe in transit)
        (a/pipe transit out))))
```
