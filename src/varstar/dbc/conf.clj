;;;; Conf provides an access point for the configuration file
;;;; and generates capfile data
;;;;
;;;;     (conf) : capistranoProtocol
;;;;     (gen-capfile conf) : nil
(ns varstar.dbc.conf
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as s]))

(def capfile-body (slurp "resources/strings/capbody.cap"))

(try (slurp "resources/strings/capbody.rb")
     (catch Exception e (.getMessage e)))

(defn- append-file! [file string]
  (spit file string :append true))

(defprotocol CapistranoProtocol
  "Required information for capistrano"
  (user [this] "Username of the account used for uploading")
  (gateway [this] "Gateway")
  (roles [this] "List of servers")
  (authentication [this] "Gets authentication scheme")
  (upload-target [this] "Gets the upload target path for Vertica"))

(deftype YamlConf [k=>v]
  CapistranoProtocol
  (user [this] (:upload-user k=>v))
  (gateway [this] (:gateway k=>v))
  (roles [this] (s/split (:servers k=>v) #",\ *"))
  (authentication [this] (:authentication k=>v))
  (upload-target [this] (:upload-target k=>v)))

(defn conf []
  (YamlConf.
   (yaml/parse-string
    (try (slurp "conf/conf.yml")
         (catch java.io.FileNotFoundException e
           (spit "conf/conf.yml"
                 (slurp "resources/strings/conf.yml.default")))))))

;; Force create blank conf.yml on initialization
(conf)

(defn- parse-nodes [nodes]
  (apply str (map #(format ", \"%s\"" %) nodes)))

(defn- build-credentials!
  ""
  [user gateway]
  (spit "Capfile"
        (str "set :user, \"" user "\"\n"
             (if (nil? gateway)
               ""
               (str "set :gateway, \"" gateway "\"\n"))
             "set :current_path, \"\"\n")))

(defn- build-roles!
  ""
  [server-list]
  (doseq [[k v] server-list]
    (append-file! "Capfile" (str "role " k
                                (parse-nodes v)
                                "\n"))))

(defn- build-funcs! []
  (append-file! "Capfile" capfile-body))

(defn gen-capfile! [conf]
  (build-credentials! (user conf) (gateway conf))
  (build-roles! (roles conf))
  (build-funcs!))
