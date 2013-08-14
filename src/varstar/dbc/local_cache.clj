;;;; Local-cache stores files on server for capistrano to read
;;;;
;;;;     (upload-file filename actual-file) : nil
(ns varstar.dbc.local-cache
  (:require [clojure.java.io :as io])
  (:import [java.io File]))

(defn- make-file
  "Wrapper for File."
  [^String path]
  (File. path))

(defn upload-file
  "Upload file to uploads folder."
  [filename actual-file]
  (io/copy actual-file (make-file (str "uploads/" filename))))


