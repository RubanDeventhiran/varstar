;;;; Query generates query strings for vertica
(ns varstar.dbc.query)

;;; If function is transform, use "transform", else use ""
(defn create-lib-query [libname filename upload-target type]
  (format "create library %s as '%s/%s' language '%s';"
          libname upload-target filename type))

(defn create-func-query [udxtype funcname factoryname libname type]
  (format "create %s function %s as language '%s' name '%s' library %s;"
          udxtype funcname type factoryname libname))

(defn drop-func-query [transform funcname arglist]
  (format "drop %s function %s(%s)"
          transform funcname arglist))

(defn drop-lib-query [libname]
  (format "drop library %s" libname))

(defn select-active-query []
  "SELECT function_name,
                     function_definition,
                     function_argument_type,
                     procedure_type
                     FROM user_functions")
