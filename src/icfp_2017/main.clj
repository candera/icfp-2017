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
  (while (not= \: (char (.read in))))
  (let [val (json/read in)]
    (log/debug "read:" (pr-str val))
    val))

(defn handshake
  [name in out]
  (log/debug "Starting handshake read")
  (send-json out {"me" name})
  (read-json in))

(defn handle-setup
  [{:strs [punter punters map] :as msg}]
  {"ready" punter
   "state" msg})

(defn riverize
  [x]
  #{(get x "source")
    (get x "target")})

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

(defn my-rivers
  [me moves]
  (->> moves
       (filter #(= me (get-in % ["claim" "punter"])))
       (map #(get % "claim"))
       (map riverize)
       set))

(defn river-sites
  [rivers]
  (reduce into #{} rivers))

(defn river-touches?
  [sites river]
  (some river sites))

(defn desirability
  [map my-sites river]
  (let [{:strs [mines]} map]
    (cond
      (river-touches? mines river)
      2

      (river-touches? my-sites river)
      1

      :else
      0)))

(defn handle-move
  [{:strs [moves state] :as msg}]
  (let [{:strs [punter punters map]} state
        our-rivers                   (my-rivers punter moves)
        [source target :as river]    (->> (unclaimed-rivers map moves)
                                          (sort-by #(desirability map (river-sites our-rivers) %))
                                          last
                                          seq)]
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

(defn run-offline
  [name in out]
  (log/info "Starting an offline game")
  (handshake name in out)
  (loop [msg (read-json in)]
    (log/debug "Entering run loop")
    (let [type    (message-type msg)
          handler (handlers type)]
      (log/debug "Received message of type" :type type :msg msg)
      (send-json out (handler msg))
      (when (#{:setup :move} type)
        (handshake name in out)
        (recur (read-json in)))))
  (log/info "Offline game has completed"))

(defn run-online
  [name host port]
  (log/info "Starting an online game")
  (let [socket (java.net.Socket. (java.net.InetAddress/getByName host) port)
        _      (log/debug "Socket connection successful")
        is     (.getInputStream socket)
        os     (.getOutputStream socket)
        in     (io/reader is)
        out    (io/writer os)]
    (handshake name in out)
    (let [{:strs [punter] :as setup} (read-json in)]
      (log/info "We are punter" :punter punter)
      (send-json out {"ready" punter})
      (loop [moves []
             msg   (read-json in)]
        (let [type (message-type msg)]
          (if (= type :move)
            (let [all-moves (into moves (get msg "moves"))]
              (send-json out (handle-move {"moves" all-moves
                                           "state" setup}))
              (recur all-moves (read-json in)))
            (do
              (log/debug "Shutting down because message was" :type type :msg msg :moves moves)
              (->> (get-in msg ["stop" "scores"])
                   (sort-by #(get % "score") )
                   (map (fn [{:strs [punter score]}]
                          (format "%10d: %d %s\n"
                                  punter
                                  score
                                  (if (= (get setup "punter") punter)
                                    "**us*"
                                    ""))))
                   (apply str (format "\n%10s: %s\n" "punter" "score"))
                   (log/info "score"))
              (.close in)
              (.close out)))))))
  (log/info "Online game has completed"))

(defn -main [& args]
  (log/info "Starting")
  (let [[host port & more]  args]
    (if host
      (run-online "drop-tables-team" host (Integer/parseInt port))
      (run-offline "drop-tables-team" *in* *out*)))
  (log/info "Terminating")
  (System/exit 0))
