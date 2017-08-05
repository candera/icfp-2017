(ns icfp-2017.main
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.tools.logging :as log]))

(defn send-json
  [out data]
  (let [encoded (json/write-str data)
        l (count encoded)
        payload (format "%d:%s" (count encoded) encoded)]
    (log/debug "sent: " payload)
    (.write out (.toCharArray payload))
    (.flush out)))

(defn read-json
  [in]
  ;; Throw away the length leader
  (while (not= \: (char (.read in)))
    (log/debug "Read throwaway character"))
  (log/debug "Found colon")
  (let [val (json/read in)]
    (log/debug "read:" (pr-str val))
    val))

(defn handshake
  [name in out]
  (log/debug "Starting handshake read")
  (send-json out {"me" name})
  (read-json in)
  ;; TODO: Verify that it's what's expected.
)

(defn handle-setup
  [{:strs [punter punters map] :as msg}]
  {"ready" punter
   "state" msg})

(defn riverize
  [x]
  #{(get x "source")
    (get x "target")})

(defn river=
  [a b]
  (= (riverize a) (riverize b)))

(defn unclaimed-rivers
  [{:strs [rivers]} moves]
  (set/difference (->> rivers
                       (map riverize)
                       set)
                  (->> moves
                       (map #(get % "claim"))
                       (remove nil?)
                       (map riverize)
                       set)))

(defn handle-move
  [{:strs [moves state] :as msg}]
  (let [{:strs [punter punters map]} state
        [source target :as river]    (seq (first (unclaimed-rivers map moves)))]
    (if river
      {"claim" {"source" source
                "target" target
                "punter" punter}
       "state" state}
      {"pass" {"punter" punter}})))

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
  [name in out]
  (log/debug "Reading handshake")
  (handshake name in out)
  (log/debug "Reading message")
  (read-json in))

(defn run
  [name in out]
  (loop [msg (read-message name in out)]
    (log/debug "Entering run loop")
    (let [type    (message-type msg)
          handler (handlers type)]
      (log/debug "Received message of type" :type type :msg msg)
      (send-json out (handler msg))
      (when (#{:setup :move} type)
        (recur (read-json in))))))

(defn run-socket
  [name host port]
  (let [socket (java.net.Socket. (java.net.InetAddress/getByName host) port)
        _      (log/debug "Socket connection successful")
        is     (.getInputStream socket)
        os     (.getOutputStream socket)]
    (run "drop-tables-team" (io/reader is) (io/writer os))))

(defn -main [& args]
  (log/info "Starting")
  (let [[host port & more]  args]
    (if host
      (run-socket "drop-tables-team" host (Integer/parseInt port))
      (run "drop-tables-team" *in* *out*)))
  (log/info "Terminating")
  (System/exit 0))
