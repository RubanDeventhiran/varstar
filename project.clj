(defproject varstar "0.7.0-SNAPSHOT"
  :description "Deployer webservice for uploading Vertica UDFs."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojure/java.jdbc "0.3.0-alpha4"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [lib-noir "0.6.6"]
                 [ring-server "0.2.8"]
                 [vertica-jdk5/vertica-jdk5 "6.1.0-0"]
                 [clj-yaml "0.4.0"]
                 [cheshire "5.2.0"]
                 ]
  :plugins [[lein-ring "0.8.6"]
            [lein-marginalia "0.7.1"]]
  :ring {:handler varstar.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.1.8"]]}})
