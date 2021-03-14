(ns org.rssys.flinkexample.core
  (:gen-class)
  (:require
    [clojure.string :as str]
    [io.pedestal.log :as log])
  (:import
    (org.apache.flink.api.common.functions
      FlatMapFunction)
    (org.apache.flink.api.java
      ExecutionEnvironment)
    (org.apache.flink.api.java.tuple
      Tuple2)
    (org.rssys.flinkexample
      WordCountTuple)))


(defn set-global-exception-hook
  "Catch any uncaught exceptions and print them."
  []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException
        [_ thread ex]
        (println "uncaught exception" :thread (.getName thread) :desc ex)))))


(def flink-env (ExecutionEnvironment/getExecutionEnvironment))

(def text (.fromElements flink-env (to-array ["please test me and me too"])))


(deftype tokenizer [] FlatMapFunction
         (flatMap [this value collector]
           (doseq [v (str/split value #"\s")]
             (.collect collector (Tuple2. v (int 1))))))


(def tokens (.returns (.flatMap text (tokenizer.)) WordCountTuple))

(def counts (.sum (.groupBy tokens (int-array [0])) 1))


(defn -main
  "entry point to app."
  [& args]
  (set-global-exception-hook)
  (log/info :msg "app is started." :args args)

  (.print counts)

  (System/exit 0))

