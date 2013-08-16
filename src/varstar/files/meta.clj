;;;; Meta provides functionality for extracting meta information
;;;; from UDF factory functions, and also performs verification.
;;;;
;;;;     (make-identifier filename actual-file factory) : meta list
;;;;     (get-collisions factory lib-maps) : {'factory' * 'library}
;;;;
;; At some point, might be worth swapping meta map with protocol
(ns varstar.files.meta
  (:require [varstar.files.meta.r :as r]
            [varstar.files.meta.cpp :as cpp]))

(defn- valid-fac-func
  "Validates that all factory functions are present.
  Does not check if every function has a factory."
  [fac func]
  (every? true? (map #(contains? func (keyword %)) fac)))

(defn make-identifier
  "Takes file info and parses meta information for creating deployment query."
  [filename fileext actual-file]
  (case fileext
    "R" (r/make-R-identifier filename actual-file)
    "so" (cpp/make-cpp-identifier filename actual-file)
    ))
  
(defn- convert-libmap-to-facmap
  "Reindexes library map to factory for collision search"
  [lib-map]
  (reduce (fn ([m k] (assoc m (:factory k) (:library k)))
              ([m] m))
          {}
          (map
           #(select-keys % [:factory :library])
           lib-map)))

(defn- tokenize-factory
  "Transforms user input factory functions (as comma separated values)
  into list of strings"
  [factory]
  (clojure.string/split factory #"\s*,\s*"))

(defn get-collisions
  "Returns collision between stated functions in factory string list,
  and in loaded functions
  {'factory' * 'library'}"
  [factory lib-maps]
  (let [facs (convert-libmap-to-facmap lib-maps)]
    (select-keys facs (tokenize-factory factory))))

