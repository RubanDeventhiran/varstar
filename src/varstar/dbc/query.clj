;;;; Query generates query strings for vertica
(ns varstar.dbc.query)

;;; If function is transform, use "transform", else use ""
(defn create-lib-query [libname filename upload-target]
  (format "create library %s as '%s/%s' language 'R';"
          libname upload-target filename))

(defn create-func-query [transform funcname factoryname libname]
  (format "create %s function %s as language 'R' name '%s' library %s;"
          transform funcname factoryname libname))

(defn drop-func-query [transform funcname arglist]
  (format "drop %s function %s(%s)"
          transform funcname arglist))

(defn drop-lib-query [libname]
  (format "drop library %s" libname))
