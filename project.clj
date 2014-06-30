(defproject vchain "0.3-SNAPSHOT"

  :description "DB --> value-chain graph model --> REST API and single-page app"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [korma "0.3.1"]
                 [postgresql "9.3-1101.jdbc4"]
                 [http-kit "2.1.16"]
                 [ring/ring "1.2.1"]
                 [enlive "1.0.0"]
                 [compojure "1.1.6"]
                 [clj-json "0.5.0"]
                 [clj-time "0.7.0"]
                 [ring-json-params "0.1.3"]
                 [ring-mock "0.1.3"]
                 [ragtime "0.3.1"]
                 [fogus/ring-edn "0.2.0"]
                 [com.cemerick/friend "0.2.0"] 
                 [com.taoensso/timbre "3.2.1"]
                 [clojure-csv/clojure-csv "2.0.1"]

                 ; log4j is here to get rid of overly verbose logging in korma; see src/log4j.xml
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]

                 ; cljx deps
                 [com.novemberain/validateur "2.1.0"]

                 ; clojurescript deps
                 [org.clojure/clojurescript "0.0-2227"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [jayq "2.5.1"]
                 [cljs-ajax "0.2.4"]
                 [om "0.6.2"]
                 [om-autocomplete "0.1.0-SNAPSHOT"]
                 [secretary "1.1.0"]
                 [markdown-clj "0.9.44"]
                 [net.drib/strokes "0.5.1"]
                 [sablono "0.2.17"]]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/java.classpath "0.2.2"]
                                  [clj-sql-up "0.3.1"]]
                   :plugins [[com.cemerick/austin "0.1.4"]]}}

  :main vchain.core

  :plugins [[clj-sql-up "0.3.1"]
            [lein-cljsbuild "1.0.3"]]

  :source-paths ["src/clj"]

  :resource-paths ["resources"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/prod"
                                   :optimizations :whitespace
                                   :source-map "resources/public/js/main.js.map"
                                   :pretty-print false}}]}

  :clj-sql-up {:database "jdbc:postgresql://postgres:postgres@localhost:5432/vchain"
               :deps [[org.clojure/java.jdbc "0.3.3"]
                      [korma "0.3.1"]
                      [clj-json "0.5.0"]
                      [clj-time "0.7.0"]
                      [postgresql "9.3-1101.jdbc4"]
                      [clojure-csv/clojure-csv "2.0.1"]
                      [com.cemerick/pomegranate "0.3.0"]                      
                      ]})

