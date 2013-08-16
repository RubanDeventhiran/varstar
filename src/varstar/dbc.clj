;;;; DBC provides a front end for vertica server communication.
;;;;
;;;;     (initialize-connection) : nil
;;;;     (get-active-UDF) : UDF list
;;;;     (upload-files-vertica funcs)
;;;;     (unload-libraries funcs loaded) : nil
;;;;     (load-libraries metadata) : nil
;;;;     (run-query query) : string
;;;;     (load-user-package filename) : nil
;;;;     (install-package package-name) : nil
(ns varstar.dbc
  (:require [varstar.dbc.conf :as c]
            [varstar.dbc.cap :as cap]
            [varstar.dbc.query :as q]
            [varstar.dbc.local-cache :as l]
            [clojure.java.jdbc :as jdbc]
            [clojure.java.jdbc.sql :as sql]))

(defn- vertica
  "Defines the Vertica connection"
  [env]
  (c/authentication (c/conf) env))

(defn- find-key
  "Boilerplate for extracting value from string"
  [regex func-def]
  (let [target (re-find regex func-def)]
    (if (nil? target)
      ""
      (last target))))

(defn- get-library [func-def]
  (find-key #"Library \'[\w]+\.([\w]+)\'" func-def))

(defn- get-factory [func-def]
  (find-key #"Class \'(\w+)\'" func-def))

(defn- get-udxtype [func-def]
  (if (nil? (re-find #"Transform" func-def))
    ""
    "Transform"))

(defn get-active-UDF
  "Gets active UDFs from vertica."
  [env]
  (let [query
        (jdbc/query (vertica env)
                    ["SELECT function_name,
                     function_definition,
                     function_argument_type,
                     procedure_type
                     FROM user_functions"])]
    (map
     (fn [e]
       ;; e contains :function_name, :function_argument_type
       (-> e
           (assoc :library (get-library (:function_definition e)))
           (assoc :factory (get-factory (:function_definition e)))
           (assoc :udxtype (get-udxtype (:procedure_type e)))))
     query)))

(defn setup-env!
  "Prepares for connection to environment"
  [env]
  (c/gen-capfile! (c/conf) env)
  (cap/update-packages! env)
  )

(defn get-path
  [^java.io.File f]
  (.getAbsolutePath f))

(defn upload-files-vertica!
  "Uploads list of files to vertica"
  [files env]
  ;;   (for [f files]
  ;;     (l/upload-file (:filename f) (:tempfile f)))
  (cap/upload-files!
   (map #(vector (:filename %) (get-path (:tempfile %))) files)
   (c/upload-target (c/conf) env)
   env))

(defn- get-unload-functions
  "Filter functions for removal query"
  [funcs loaded]
  (let [f-name-pool (map #(:name %) funcs)
        f-lib-pool (map #(:library %) funcs)]
    (filter #(or
              (some #{(:function_name %)} f-name-pool)
              (some #{(:library %)} f-lib-pool)) loaded)))

(defn- get-unload-libraries
  "Filter libraries for removal query"
  [funcs loaded]
  (let [f-lib-pool (map #(:library %) funcs)
        loaded-lib (map #(:library %) loaded)]
    ;; Use set to find unique entries
    (set (filter #(some #{%} f-lib-pool) loaded-lib))))

(defn- unload-func!
  [func env]
  (let [query (q/drop-func-query
               (:udxtype func)
               (:function_name func)
               (let [args (:function_argument_type func)]
                 (if (= args "Any")
                   ""
                   args)))]
    (jdbc/execute! (vertica env) [query])))

(defn- unload-lib!
  [lib env]
  (let [query (q/drop-lib-query lib)]
    (jdbc/execute! (vertica env) [query])))

(defn unload-libraries!
  "Unloads libraries and functions to remove conflicts with incoming uploads"
  [funcs loaded env]
  (let [ul-func-list (get-unload-functions funcs loaded)
        ul-lib-list (get-unload-libraries funcs loaded)]
    (if (< 0 (count ul-func-list))
      (doall
       (for [f ul-func-list]
         (unload-func! f env))))
    (if (< 0 (count ul-lib-list))
      (doall
       (for [l ul-lib-list]
         (unload-lib! l env))))))

(defn- get-libraries
  "From metadata extract unique libraries to upload"
  [metadata]
  (set (map #(select-keys
              %
              [:library :type :filename])
            metadata)))

(defn- load-lib! [library env]
  (let [query (q/create-lib-query
               (:library library)
               (:filename library)
               (c/upload-target (c/conf) env)
               (:type library))]
    (prn query)
    (jdbc/execute! (vertica env) [query])))

(defn- load-func! [metadata env]
  (let [query (q/create-func-query
               (case (:udxtype metadata)
                 "transform" "transform"
                 "scalar" "")
               (:name    metadata)
               (:factory metadata)
               (:library metadata)
               (:type    metadata))]
    (prn query)
    (jdbc/execute! (vertica env) [query])))

(defn load-libraries!
  "Loads libraries as per provided metadata, then loads functions"
  [metadata env]
  (let [lib-list (get-libraries metadata)]
    (if (< 0 (count lib-list))
      (doall (for [l lib-list]
               (load-lib! l env))))
    (if (< 0 (count metadata))
      (doall (for [f metadata]
               (load-func! f env)))))
  )

;; So far, (jdbc/query) does a pretty good job. It only allows one
;; statement, and does not execute anything that updates the database.
(defn- sanitize [query]
  query)

(defn run-query
  "Executes query, testing for success status"
  [query env]
  (let [sanitized-query (sanitize query)]
    (try
      ;;;TODO: sanitize the query
      (jdbc/query (vertica env) [sanitized-query]
                  :result-set-fn (fn [& _] "Query successful"))
      (catch Exception e (str (.getMessage e))))))

(defn- run-update
  "Executes query, testing for success status"
  [query env]
  (try
    (jdbc/execute! (vertica env) [query]
                   :result-set-fn (fn [& _] "Query successful"))
    (catch Exception e (str (.getMessage e))))
  )

;; (run-query "drop library testLib" "load")
;; (run-update "drop library testLib" "load")

(defn load-user-package!
  [filename env]
  (cap/load-custom-package! filename env))

;; N.B. currently watered down to only handle one package at a time
(defn install-package!
  "Install package for R-code use"
  [package-name]
  (try
    (cap/install-packages! '(package-name))
    (catch Exception e (.getMessage e))))