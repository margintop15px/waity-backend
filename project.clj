(defproject waity-backend "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [duct/core "0.8.0"]
                 [io.github.parencat/fx "0.1.4"]
                 [com.google.zxing/core "3.5.1"]
                 [com.google.zxing/javase "3.5.1"]
                 [metosin/reitit "0.5.18"]
                 [metosin/ring-http-response "0.9.3"]
                 [ring/ring-jetty-adapter "1.9.6"]]

  :plugins [[duct/lein-duct "0.12.3"]]
  :middleware [lein-duct.plugin/middleware]

  :main ^:skip-aot waity-backend.main
  :resource-paths ["resources" "target/resources"]

  :profiles
  {:dev     {:source-paths   ["dev/src"]
             :resource-paths ["dev/resources"]
             :dependencies   [[integrant/repl "0.3.2"]
                              [hawk "0.2.11"]
                              [eftest "0.6.0"]]}

   :repl    {:repl-options {:init-ns dev}}

   :uberjar {:aot :all}})
