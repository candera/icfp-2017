(ns icfp-2017.main
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defn send-json
  [out data]
  (log/info "sent: " (pr-str data))
  (json/write out data))

(defn read-json
  [in]
  (let [val (json/read in)]
    (log/info "read:" (pr-str val))
    val))

(defn handshake
  [name in out]
  (send-json out {"me" name})
  (read-json in)
  ;; TODO: Verify that it's what's expected.
)

(defn handle-setup
  [{:strs [punter punters map] :as msg}]
  {"ready" punter
   "state" msg})

(defn handle-move
  [{:strs [moves state] :as msg}]
  (let [{:strs [punter punters map]} state]
    {"claim" {"punter" punter
              ;; TODO:
              "source" 42
              "target" 1024}}))

(defn handle-stop
  [{:strs [stop]}]
  (let [{:strs [moves scores] :as msg} stop]
    ;; Doesn't really matter what we return
    msg))

(defn message-type
  [msg]
  (cond
    (contains? msg "punter") :setup
    (contains? msg "move")   :move
    (contains? msg "stop")   :stop))

(def handlers
  {:setup handle-setup
   :move  handle-move
   :stop  handle-stop})

(defn read-message
  [in out]
  (handshake in out)
  (read-json in))

(defn run
  [in out]
  (loop [msg (read-message in out)]
    (let [type (message-type msg)
          handler (handlers type)]
      (send-json out (handler msg))
      (when-not (= type :stop)
        (recur (read-json in))))))

#_(proxy [java.io.Reader] []
  (close [])
  (mark [readAheadLimit])
  (markSupported [])
  (read
    ([])
    ([cbuf])
    ([cbuf offset len]))
  (ready [])
  (reset [])
  (skip [n]))

(ancestors (class *in*))


