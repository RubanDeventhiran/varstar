(ns varstar.views.common
  (:use [hiccup.def :only [defhtml]]
        [hiccup.page :only [html5 include-css include-js]]))

(defhtml layout [& body]
  (html5
   [:head
    [:title "Welcome to myapp"]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    (include-css
     "css/bootstrap.css"
     "css/style.css")
    (include-js
     "http://code.jquery.com/jquery-1.9.1.js"
     "http://code.jquery.com/ui/1.10.3/jquery-ui.js"
     "script/bootstrap.js"
     "script/upload-script.js"
     "script/status.js"
     "script/tabs.js"
     "script/package.js"
     "script/query.js"
     "script/main.js")]
   (into [:body] body)))

