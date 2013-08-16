;;;; Meta.r provides R functionality for extracting UDF identifying info
;;;; from factory functions
;;;;
;;;;     (make-R-identifier filename actual-file) : meta list
;;;;
(ns varstar.files.meta.r)

(defn- remove-comments [fn-body]
  (clojure.string/replace fn-body #"#.*\n" ""))

(defn- normalize-whitespace [fn-body]
  (clojure.string/replace fn-body #"\s+" " "))

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

(defn- is-factory
  "Determines if function is factory function,
  by checking for name and udxtype keys."
  [func]
  ;; get body, read for factory parts
  (let [body (last func)]
    (not (or (nil? (re-find #"name=" body))
             (nil? (re-find #"udxtype=" body))))))

(defn make-R-identifier
  "Creates a map for each function in the file, containing
- name
- udxtype
- library
- type
- filename
- factory"
  [filename actual-file]
  (let [func (tokenize-functions (remove-comments (slurp actual-file)))]
    (reduce
     (fn [funclist f]
       (cons (->
              ;; Contains :name, :udxtype
              (extract-factory (last f))
              (assoc :library filename)
              (assoc :type "R")
              (assoc :filename (str filename ".R"))
              (assoc :factory (name (first f))))
             funclist))
     '()
     (filter is-factory func))))
