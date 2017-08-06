(merge-env!
 :resource-paths #{"src"}
 :source-paths   #{"test"}

 :dependencies   (template [[org.clojure/clojure ~(clojure-version)]
                            [org.clojure/data.json "0.2.6"]
                            [org.clojure/tools.logging "0.4.0"]
                            [log4j "1.2.17"]

                            [adzerk/boot-test "1.2.0" :scope "test"]])

 ;; These support Craig's repl server workflow
 :repl-server-port 3039
 :repl-server-name "icfp-repl")

(require '[icfp-2017.main :as main])
(require '[clojure.data.json :as json])
(require '[clojure.java.io :as io])
(require '[clojure.tools.logging :as log])
(require '[clojure.test])
(require 'icfp-2017.test)
(require '[adzerk.boot-test :refer :all])

(defn refresh []
  (require :reload-all '[icfp-2017.main :as main]))

(deftask build
  []
  (comp (uber)
        (jar :file "punter.jar")
        (sift :include #{#"^punter.jar$"
                         #"^punter$"
                         #"^install$"})
        (target)))
