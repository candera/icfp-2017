(ns icfp-2017.main
  (:require [clojure.data.json :as json]
            [clojure.set :as set]
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

(defn riverize
  [x]
  (select-keys x ["source" "target"]))

(defn river=
  [a b]
  (= (riverize a) (riverize b)))

(defn available-rivers
  [map moves]
  (set/difference (get map "rivers")
                  (map riverize moves)))

(defn handle-move
  [{:strs [moves state] :as msg}]
  (let [{:strs [punter punters map]} state
        river (first (available-rivers map moves))]
    (if river
      {"claim" (assoc river "punter" punter)
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

(defn -main [& args]
  (println "args: " args)
  (System/exit 0))
