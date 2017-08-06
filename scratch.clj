(let [ map0  {"sites"  [{"id" 0}
                        {"id" 1}]
              "rivers" [{"source" 0
                         "target" 1}]
              "mines"  [0 1]}
      punter 1
      state  {"punter"  punter
              "punters" 2
              "map"     map0}]
  (main/handle-move {"moves" []
                     "state" state}))

(let [ map0  {"sites"  [{"id" 0}
                        {"id" 1}]
              "rivers" [{"source" 0
                         "target" 1}]
              "mines"  [0 1]}
      punter 1
      moves [{"claim" {"punter" 2
                       "source" 0
                       "target" 1}}]
      ;;moves []
      state  {"punter"  punter
              "punters" 2
              "map"     map0}]
  (set/difference (set (get map0 "rivers"))
                  (->> moves
                       (map #(get % "claim"))
                       (remove nil?)
                       (map main/riverize)
                       set))
  (main/unclaimed-rivers map0 moves)
  #_(main/handle-move {"moves" moves
                       "state" state}))

(let [[a b :as c] (seq (first #{}))]
  [a b c])

(let [sr (java.io.StringReader. "1234:{\"blah\":3}12341234:{}")]
  (main/read-json sr)
  (main/read-json sr))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(main/run-online "bar" "punter.inf.ed.ac.uk" 9185)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [map1   {"sites"  [{"id" 0}
                        {"id" 1}
                        {"id" 2}
                        {"id" 3}]
              "rivers" [{"source" 0
                         "target" 1}
                        {"source" 1
                         "target" 2}
                        {"source" 2
                         "target" 3}]
              "mines"  [0]}
      punter 0
      moves  [{"claim" {"punter" 0
                        "source" 0
                        "target" 1}}
              {"claim" {"punter" 1
                        "source" 1
                        "target" 2}}
              {"claim" {"punter" 2
                        "source" 3
                        "target" 2}}]
;;      moves []
      state  {"punter"  punter
              "punters" 2
              "map"     map1}]
  #_(->> moves
       (filter #(= punter (get-in % ["claim" "punter"])))
       #_(map main/riverize)
       set)
  #_(main/my-rivers punter moves)
  (main/handle-move {"moves" moves
                     "state" state}))
