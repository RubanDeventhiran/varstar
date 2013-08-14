;;;; Provides functionality for converting clojure objects into
;;;; equivalent html elements
;;;;
;;;;     (derive-col-order list-of-maps) : col-order
;;;;     (make-table list-of-maps col-order) : html
(ns varstar.views.tables
  (:require [hiccup.core :as hic]
            [hiccup.def :as hdef])
  )

(defn derive-col-order
  "Returns a column order based on the keys in
any of the maps. It's assumed the maps all have
exactly the same keys"
  [list-of-maps]
  ;; this could change and just be a hard coded
  ;; or configured value
  (keys (first list-of-maps)))

(defn- make-table-data-cell [k=>v k]
  [:td (k=>v k)])
(defn- make-table-header-cell [_ k]
  [:th k])

(defn- make-table-row [k=>v key-order data-cell-maker]
  (vec (cons :tr (mapv #(data-cell-maker k=>v %) key-order))))

(defn- make-header-row [key-order]
  (make-table-row nil key-order make-table-header-cell))

(defn- make-data-row [k=>v key-order]
  (make-table-row k=>v key-order make-table-data-cell))

(defn make-table [list-of-maps col-order]
  (vec (concat
   [:table
    (make-header-row col-order)]
   (map #(make-data-row % col-order) list-of-maps))))


(comment
  ;; This is the structure we're trying to build
  ;; where each coln is the key in the map
  ;; and each datan is the value corresponding
  ;; to the coln-key in the map
[:table
 [:tr [:th col1] [:th col2] [:th coln]]
 [:tr [:td data1] [:td data2] [:td datan]]
 [:tr [:td data1] [:td data2] [:td datan]]
 [:tr [:td data1] [:td data2] [:td datan]]
 [:tr [:td data1] [:td data2] [:td datan]]]
)
