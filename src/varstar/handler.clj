;;;; Handler provides core functionality, and is entry point for
;;;; the web app.
;;;;
;;;; Handler accesses pageviews, files, database, and config
(ns varstar.handler
  (:use compojure.core)
  (:require [varstar.views :as v]
            [varstar.files :as f]
            [varstar.dbc :as d]
            (ring.middleware [multipart-params :as mp])
            [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as hic]
            [cheshire.core :as json]))

;; Default output status message
(def default "Awaiting files")

(defn filter-funcs [funcs filter-list]
  (filter
   (fn [f] (some #{(str (:library f) "." (:factory f))} filter-list))
   funcs))

(defn- deploy!
  ""
  [session clear filter-list]
  ; Read in meta files, create list of files and queries to execute
  (try
    (let [files (f/get-file-list session)
          fcs (f/get-identifiers session)
          active-functions (filter-funcs fcs filter-list)
          loaded (d/get-active-UDF)]
      (println "Setting up connection...")
      (d/setup-conn!)
      (println "Uploading files...")
      (d/upload-files-vertica! (:files session))
      (println "Unloading conflicting libraries...")
      (d/unload-libraries! active-functions loaded)
      (print "Loading libraries...")
      (d/load-libraries! active-functions)
      (if (nil? clear)
        (do
          (println "done.")
          {:status {:out "Library loaded"}
           :session session})
        (do
          (print "\nClearing local files...")
          (println "done.")
          {:status {:out "Library loaded and workspace cleared"}
           :session (f/clear-files session)})))
    (catch Exception e
      {:status {:out (str (.getMessage e))}
       :session session})))

(defn- load-user-package! [file]
  (if (< 0 (:size file))
    (try
      (println "Uploading package...")
      (d/upload-files-vertica! '(file))
      (print "Loading file to R...")
      (d/load-user-package! (:filename file))
      (println "done.")
      "Upload successful"
      (catch Exception e (str (.getMessage e)))
      )
    "No package to load"))

(defn- page [output]
  (v/page-main (:status output)
               (f/get-func-list (:session output))
               (f/get-file-list (:session output))
               (try (d/get-active-UDF)
                 (catch Exception e
                   (str (.getMessage e))))))

(defn- response [output]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (:status output)
   :session (:session output)})

(defn- ajax-response [output]
  (let [body (:status output)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (json/generate-string body)
     :session (:session output)}))

(defroutes app-routes
  ;; Session stores file names as keys, and stores

  (GET "/" {session :session
            params :params}
       (let [output {:status (page default)
                     :session {:files '()
                               :meta '()}}]
         (response output)))

  (GET "/loaded" {session :session
                  params :params}
       (let [output {:status (try
                               ;; Switch with ajax
                               (v/load-html (d/get-active-UDF))
                               (catch Exception e (.getMessage e)))
                     :session session}]
         (response output)))

  (mp/wrap-multipart-params
   (POST "/upload" {session :session
                    params :params}
         (prn (.getAbsolutePath (:tempfile (:file params))))
         (let [file (get params :file)
               output (if (string? file)
                        {:session session
                         :status {:out "No file to upload"
                                  :data nil}}
                        (f/process-file
                         session
                         (get params :file)))]

           (ajax-response output))))
  (POST "/clear" {session :session
                  params :params}
        (print "Clearing files...")
        (let [output (f/clear-files session)]
          (println "done.")
          (ajax-response output)))
  (mp/wrap-multipart-params
   (POST "/deploy" {session :session
                    params :params}
         (prn params)

         (println "Executing deployment...")
         (let [output (deploy! session
                              (#{"true"} (get params :clear))
                              (get params :filter))]
           (ajax-response output))))
  (mp/wrap-multipart-params
   (POST "/query" {session :session
                   params :params}
         (println "Executing query: \"" (get params :query) "\"")
         (let [output {:status (d/run-query (get params :query))
                       :session session}]
           (ajax-response output))))
  (mp/wrap-multipart-params
   (POST "/package" {session :session
                     params :params}
         (println "Loading user package...")
         (let [file (get params :file)
               output {:session session
                       :status
                       (if (string? file)
                         {:out "No package to upload"}
                         {:out (load-user-package!
                                file)})}]
           (ajax-response output))))
  (POST "/install" {session :session
                    params :params}
        (let [output {:status {:out (d/install-package! (:package params))}
                      :session session}]
          (ajax-response output)))

  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
