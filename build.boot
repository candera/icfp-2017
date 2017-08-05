(merge-env!
 :source-paths   #{"src" "test"}

 :dependencies   (template [[org.clojure/clojure ~(clojure-version)]
                            [org.clojure/data.json "0.2.6"]
                            [org.clojure/tools.logging "0.4.0"]

                            [adzerk/boot-test "1.2.0" :scope "test"]])

 ;; These support Craig's repl server workflow
 :repl-server-port 3039
 :repl-server-name "icfp-repl")

(require '[icfp-2017.main :as main])
(require '[clojure.data.json :as json])
(require '[clojure.tools.logging :as log])
(require '[clojure.test])
(require 'icfp-2017.test)
(require '[adzerk.boot-test :refer :all])

(defn refresh []
  (require :reload-all '[icfp-2017.main :as main]))

#_(defn run-tests []
  (clojure.test/run-tests 'icfp-2017.test))
