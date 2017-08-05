(ns icfp-2017.test
  (:require  [clojure.test :as t :refer [deftest is]]
             [icfp-2017.main :as main :refer :all]))

(def map0 {"sites"  [{"id" 0}
                     {"id" 1}]
           "rivers" [{"source" 0
                      "target" 1}]
           "mines"  [0 1]})

(defn valid-move?
  [move]
  (or (contains? move "claim")
      (contains? move "pass")))

(defn state-valid?
  [move]
  (contains? move "state"))

(deftest smoke-test
  (let [move (handle-move {"moves" []
                           "state" {"punter" 1
                                    "punters" 2
                                    "map" map0}})]
    (is (valid-move? move))
    (is (state-valid? move))))



