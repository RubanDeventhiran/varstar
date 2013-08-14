;;;; Conf provides an access point for the configuration file
;;;; and generates capfile data
;;;;
;;;;     (conf) : capistranoProtocol
;;;;     (gen-capfile conf env) : nil
(ns varstar.dbc.conf
  (:require [clj-yaml.core :as yaml]))

(def capfile-body (slurp "resources/strings/capbody.cap"))

(defn- append-file! [file string]
  (spit file string :append true))

(defn- sanitize-env [env]
  (if (= env "prod") :prod :load))

(defprotocol CapistranoProtocol
  "Required information for capistrano"
  (user [this env] "Username of the account used for uploading")
  (gateway [this env] "Gateway")
  (roles [this] "Lists of servers, for both prod and load")
  (authentication [this env] "Gets authentication scheme for given env")
  (upload-target [this env] "Gets the upload target path for Vertica"))

(deftype YamlConf [k=>v]
  CapistranoProtocol
  (user [this env]
        (let [e (sanitize-env env)]
          (-> k=>v
              (e)
              (:upload-user))))
  (gateway [this env]
           (let [e (sanitize-env env)]
             (-> k=>v
                 (e)
                 (:gateway))))
  (roles [this]
         {:load (:roles (:load k=>v))
          :prod (:roles (:prod k=>v))})
  (authentication [this env]
                  (let [e (sanitize-env env)]
                    (-> k=>v
                        (e)
                        (:authentication))))
  (upload-target [this env]
                 (let [e (sanitize-env env)]
                   (-> k=>v
                       (e)
                       (:upload-target)))))

(defn conf []
  (YamlConf. (yaml/parse-string (slurp "conf.yml"))))

(defn- parse-nodes [nodes]
  (apply str (map #(format ", \"%s\"" %) nodes)))

(defn- build-credentials!
  ""
  [user gateway]
  (spit "Capfile"
        (str "set :user, \"" user "\"\n"
             "set :gateway, \"" gateway "\"\n"
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

(defn gen-capfile! [conf env]
  (build-credentials! (user conf env) (gateway conf env))
  (build-roles! (roles conf))
  (build-funcs!))

