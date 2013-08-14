(ns varstar.files.zip
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [java.io File]))

(defn- make-file
  "Wrapper for File."
  [^String path]
  (File. path))

(defn- entries
  "Get entries from zipfile"
  [^java.util.zip.ZipFile zipfile]
  (enumeration-seq (.entries zipfile)))

(defn- isDirectory
  "Wrapper for .isDirectory"
  [^java.util.zip.ZipEntry e]
  (.isDirectory e))

(defn- getName
  "Wrapper for .getName"
  [^java.util.zip.ZipEntry e]
  (.getName e))

(defn- getInputStream
  "Wrapper for .getInputStream"
  [^java.util.zip.ZipFile z ^java.util.zip.ZipEntry e]
  (.getInputStream z e))


(defn- mkdirs
  "Wrapper for .mkdirs"
  [^java.io.File f]
  (.mkdirs f))


(defn- remove-extension
  "Remove file extention from filename"
  [path]
  ((string/split path #"\.") 0))

(defn- walkzip [fileName]
  (let [fileDir (remove-extension fileName)
        filePath (str "uploads/" fileName)]
    ; Create root dir
    (mkdirs (make-file (str "uploads/" fileDir)))
    ; Open the file as zip file
    (with-open [z (java.util.zip.ZipFile. filePath)]
      ; Map over entries in zip file
      (doseq [e (entries z)]
        ; Clobber together entry path
        (let [entry-path (str "uploads/" fileDir "/" (getName e))]
          (if (isDirectory e)
            ; Make directory if entry is directory
            (mkdirs (make-file entry-path))
            (io/copy (getInputStream z e) (make-file entry-path))))))))

(defn unzip-file [filename]
  (walkzip filename)
  (str filename " unzipped"))