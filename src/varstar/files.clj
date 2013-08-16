;;;; Files provides entry point for the file storage, access,
;;;; and processing.
;;;;
;;;; Files uses meta.
;;;;
;;;;     (get-file-list session)      : file list
;;;;     (get-identifiers  session)      : meta list
;;;;     (get-func-list session)      : string list
;;;;     (clear-files   session)      : output
;;;;     (process-file
;;;;         session file factory lib-meta) : output
(ns varstar.files
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [varstar.files.meta :as m])
  )

(defn- output
  "Aggregates output for handler"
  ([session status]
   {:session session :status {:out status :data nil}})
  ([session status data]
   {:session session :status {:out status :data data}}))

(defn- get-list
  "Boilerplate for extracting from session"
  [session subgroup tag]
  (map #(tag %) (subgroup session)))

(defn get-file-list [session]
  (get-list session :files :filename))

(defn get-func-list [session]
  (get-list session :meta :name))

(defn get-identifiers
  "Reads meta information and returns a meta list to load"
  [session]
  (:meta session))

(defn clear-file [file]
  (io/delete-file (str "uploads/" file)))

(defn clear-files
  "Clears files from session"
  [session]
  (output
   {:files '()
    :meta '()},
   "Files cleared"))

(defn- remove-extension
  "Remove file extention from filename"
  [path]
  ((string/split path #"\.") 0))

(defn- get-extension
  "Get file extension from filename"
  [path]
  (last (string/split path #"\.")))

(defn- if-exists [session subgroup]
  (if-let [sg (subgroup session)]
    sg
    '()))

(defn- remove-collision
  "Remove new from old to replace functions."
  [metalist new-metalist]
  (let [blacklist (map :library new-metalist)]
    (filter #(not (some #{(:library %)} blacklist)) metalist)))

(defn- add-to-session
  [session file metainfo]
  (let [files (if-exists session :files)
        metalist (if-exists session :meta)
        diff-metalist (remove-collision metalist metainfo)]
    (-> session
        (assoc :files (cons file files))
        (assoc :meta (concat metainfo diff-metalist)))))

(defn- already-in-session? [filename session]
  (let [filenames (map :filename (:files session))]
    (some #{filename} filenames)))

(defn process-file
  "Save file and pull meta information for file"
  [session file]
  (let [filename    (:filename file)
        shortfn     (remove-extension filename)
        fileext     (get-extension filename)
        size        (:size file)
        actual-file (:tempfile file)
        metadata    (m/make-identifier
                     shortfn
                     fileext
                     actual-file)]
    (if (< 0 (count metadata))
      (let [new-session (add-to-session session file metadata)]
        (prn new-session)
        (output
         new-session
         (if (already-in-session? filename session)
           (str "File \"" filename "\" uploaded " size " bytes. ")
           (str "File \"" filename "\" replaced " size " bytes. "))
         (map #(str (:library %) "." (:factory %)) (:meta new-session))))
      (output session "Could not find factory functions in file."))))