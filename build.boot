(merge-env!
 :source-paths   #{"src"}

 :dependencies   (template [[org.clojure/clojure ~(clojure-version)]
                            [org.clojure/data.json "0.2.6"]
                            [org.clojure/tools.logging "0.4.0"]])

 ;; These support Craig's repl server workflow
 :repl-server-port 3039
 :repl-server-name "icfp-repl")

(require '[icfp-2017.main :as main])
(require '[clojure.data.json :as json])
(require '[clojure.tools.logging :as log])

(defn refresh []
  (require :reload-all '[icfp-2017.main :as main]))

(defn exercise-setup []
  (main/game "foo"
             (java.io.StringReader. (apply str (map json/write-str [{"you" "foo"}
                                                                    {"punter"  1
                                                                     "punters" 3
                                                                     "map"     {"sites"  [{"id" 0}
                                                                                          {"id" 1}]
                                                                                "rivers" [{"source" 0
                                                                                           "target" 1}]
                                                                                "mines"  [0 1]}}])))
             *out*))
