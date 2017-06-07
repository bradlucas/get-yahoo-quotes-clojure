(defproject get-yahoo-quotes "1.0.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.6.1"]
                 [org.apache.commons/commons-lang3 "3.5"]]
  :profiles {:uberjar {:uberjar-name "get-yahoo-quotes.jar"}}
  :main get-yahoo-quotes.core)
