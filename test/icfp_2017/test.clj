(ns icfp-2017.test
  (:require  [clojure.test :as t :refer [deftest is]]
             [icfp-2017.main :as main :refer :all]))

(def map0 {"sites"  [{"id" 0}
                     {"id" 1}]
           "rivers" [{"source" 0
                      "target" 1}]
           "mines"  [0 1]})

(defn valid-claim?
  [prior-state claim]
  (let [{:strs [punter source target]} claim]
    (and punter source target (= punter (get prior-state "punter")))))

(defn valid-pass?
  [prior-state pass]
  (let [{:strs [punter]} pass]
    (and punter (= punter (get prior-state "punter")))))

(defn valid-move?
  [prior-state move]
  (cond
    (contains? move "claim")
    (valid-claim? prior-state (get move "claim"))

    (contains? move "pass")
    (valid-pass? prior-state (get move "pass"))

    :else
    false))

(defn state-valid?
  [prior-state move]
  (contains? move "state"))

(deftest some-tests
  (let [punter 0
        state  {"punter"  punter
                "punters" 2
                "map"     map0}]
    ;; When selecting the first move, it claims the only available
    ;; river
    (let [move (handle-move {"move" {"moves" []}
                             "state" state})]
      (is (valid-move? state move))
      (is (state-valid? state move))
      (is (= #{0 1} #{(get-in move ["claim" "source"])
                      (get-in move ["claim" "target"])})))
    ;; When all rivers have been claimed, it passes
    (let [move (handle-move {"move" {"moves" [{"claim" {"punter" 2
                                                         "source" 0
                                                         "target" 1}}]}
                             "state" state})]
      (is (valid-pass? state (get move "pass"))))
    ;; When all rivers have been claimed, it passes, even if the order
    ;; of rivers in the previous move is reversed
    (let [move (handle-move {"move" {"moves" [{"claim" {"punter" 2
                                                         "source" 1
                                                         "target" 0}}]}
                             "state" state})]
      (is (valid-pass? state (get move "pass"))))))



