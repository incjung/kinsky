(defproject spootnik/kinsky "0.1.15"
  :description "MapR [Streams/Kafka] clojure client library"
  :plugins [[lein-codox "0.9.1"]]
  :url "https://github.com/incjung/kinsky"
  :license {:name "MIT License"
            :url  "https://github.com/pyr/kinsky/tree/master/LICENSE"}
  :codox {:source-uri "https://github.com/incjung/kinsky/blob/{version}/{filepath}#L{line}"
          :metadata   {:doc/format :markdown}}
  :dependencies [[org.clojure/clojure            "1.8.0"]
                 [org.clojure/core.async         "0.2.385"]
                 [org.apache.kafka/kafka-clients "0.9.0.0-mapr-1607"]
                 [com.mapr.streams/mapr-streams  "5.2.0-mapr"]
                 [cheshire                       "5.6.3"]]
  :repositories [["java.net" "http://download.java.net/maven/2"]
                 ["mapr-releases" {:url "http://repository.mapr.com/maven"
                                   :snapshots false}]])
