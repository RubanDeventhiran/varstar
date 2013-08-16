;;;; Meta provides functionality for extracting meta information
;;;; from R script factory functions, and also performs verification.
;;;;
;;;;     (make-identifier filename actual-file factory) : meta list
;;;;     (get-collisions factory lib-maps) : {'factory' * 'library}
(ns varstar.files.meta
  (:require [clj-yaml.core :as yaml]))

(defn- remove-comments [fn-body]
  (clojure.string/replace fn-body #"#.*\n" ""))

(defn- normalize-whitespace [fn-body]
  (clojure.string/replace fn-body #"\s+" " "))

(defn- tokenize-factory
  "Transforms user input factory functions (as comma separated values)
  into list of strings"
  [factory]
  (clojure.string/split factory #"\s*,\s*"))

(defn- tokenize-functions
  "Takes a corpus of functions and returns mapping from
  function names to function body
  Returns ``{:name :body}`` list"
  [fn-body]
  ; Really should be lfold instead of reduce, ('b * 'a) -> 'b
  (reduce (fn [h [_ k v]]
            (cons [(keyword k) (normalize-whitespace v)] h))
          '()
          (re-seq #"(\w+) ?<- ?function\(.*\)\s*\{([\w\s\p{Punct}&&[^{}]]+)\}"
                  fn-body)))


(defn- extract-factory
  "Gets the function name and type from the factory declaration"
  [factory-body]
  {:name (get (first (re-seq #"name=(\w+)" factory-body)) 1)
   :udxtype (get (first (re-seq #"udxtype=c\(\"(\w+)\"\)" factory-body)) 1)
   })

(defn- valid-fac-func
  "Validates that all factory functions are present.
  Does not check if every function has a factory."
  [fac func]
  (every? true? (map #(contains? func (keyword %)) fac)))

(defn- is-factory
  "Determines if function is factory function,
  by checking for name and udxtype keys."
  [func]
  ;; get body, read for factory parts
  (let [body (last func)]
    (not (or (nil? (re-find #"name=" body))
             (nil? (re-find #"udxtype=" body))))))

(defn- fileext-to-udf-type [fileext]
  (case fileext
    "R" "R"
    "so" "C++"
    ;; Should not hit default statement
    ""))

(defn- make-R-identifier
  [filename actual-file]
  (let [func (tokenize-functions (remove-comments (slurp actual-file)))]
    (reduce
     (fn [funclist f]
       (cons (-> (extract-factory (last f))
                 (assoc :library filename)
                 (assoc :type "R")
                 (assoc :filename (str filename ".R"))
                 (assoc :factory (name (first f))))
             funclist))
     '()
     (filter is-factory func))))

(defn- make-cpp-identifier
  [filename actual-file]
  (throw (Exception. "Unimplemented")))

(defn make-identifier
  "Takes file info and parses meta information for creating deployment query."
  [filename fileext actual-file]
  (case fileext
    "R" (make-R-identifier filename actual-file)
    "so" (make-cpp-identifier filename actual-file)
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

(defn get-collisions
  "Returns collision between stated functions in factory string list,
  and in loaded functions
  {'factory' * 'library'}"
  [factory lib-maps]
  (let [facs (convert-libmap-to-facmap lib-maps)]
    (select-keys facs (tokenize-factory factory))))