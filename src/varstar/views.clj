;;;; Views provides dynamic html embedded with content
;;;;     (page-main) : nil
(ns varstar.views
  (:require [hiccup.core :as hic]
            [hiccup.def :as hdef]
            [varstar.views.tables :as t]
            [varstar.views.common :as c])
  )

(defn- collision [funcname loaded]
  (let [loaded-names (map #(:function_name %) loaded)]
    (if (nil? (some #{funcname} loaded-names))
      false
      true))
  )

(defn- navbar []
  [:div {:class "navbar"}
   [:ul {:class "nav navbar-nav nav-tabs"}
    [:li [:a {:href "#upload"
              :data-toggle "tab"}
          "Upload Files"]]
    [:li [:a {:href "#query"
              :data-toggle "tab"}
          "Query"]]
    [:li [:a {:href "#package"
              :data-toggle "tab"}
          "Install library"]]]
   [:div {:class "btn-group"
          :id "input-env"
          :data-toggle "buttons"}
    [:label {:class "btn btn-default active"}
     [:input {:type "radio"
              :name "env"
              ;; Can't use load, capistrano rejects it.
              :value "dev"} "Load"]]
    [:label {:class "btn btn-default"}
     [:input {:type "radio"
              :name "env"
              :value "prod"} "Prod"]]]]
  )

(defn- sidebar []
  [:div {:class "col-12 col-sm-4 col-lg-4"}
   [:h3 "Loaded functions"]
   [:div {:class "sidebar-content"
          :id "sidebar-content"}]])

(defn- status-bar []
  [:div {:class "status lead"
         :id "status"}
   [:p status]])

(defn- upload-tab []
  [:div {:id "upload"
         :class "tabdiv"}

   [:h4 "Upload R file here."]
   [:input {:name "file"
            :type "file"
            :size "20"
            :id "input-file-upload"}]
   [:br]
   [:input {:type "checkbox"
            :name "clear"
            :id "input-check-clear"}
    "Clear after deploy?"]
   [:br]
   [:div {:class "btn-group actions"}
    [:input {:type "submit"
             :name "submit"
             :class "btn btn-default"
             :value "Upload"
             :id "input-action-upload"}]
    [:input {:type "submit"
             :name "submit"
             :value "Deploy"
             :class "btn btn-default"
             :id "input-action-deploy"}]
    ;;; TODO: rm clear button
    [:input {:type "submit"
             :name "submit"
             :value "Clear"
             :class "btn btn-default"
             :id "input-action-clear"}]
    ]

   [:div {:class "fileList"
          :id "fileList"}]])

(defn- query-tab []
  [:div {:id "query"
         :class "tabdiv"}
   [:h4 "Test queries"]
   [:textarea {:name "query"
               :id "input-text-query"
               :class "form-control"
               :cols "35"
               :rows "5"}
    ""]
   [:br]
   [:input {:type "submit"
            :name "submit"
            :id "input-action-query"
            :class "btn btn-default"
            :value "Query"}]])

(defn- library-tab []
  [:div {:id "package"
         :class "tabdiv"}
   [:h4 "Load user package"]
   [:input {:type "file"
            :name "file"
            :id "input-file-package"
            :size "20"}]
   [:input {:type "submit"
            :name "submit"
            :id "input-action-package"
            :class "btn btn-default"
            :value "submit"}]
   [:h4 "Install CRAN package"]

   [:input {:type "text"
            :id "input-text-library"
            :name "package"}]
   [:input {:type "submit"
            :name "submit"
            :id "input-action-library"
            :class "btn btn-default"
            :value "Install"}]])

(defn- tab-content []
  [:div {:id "main-container"
         :class "col-12 col-sm-8 col-lg-8"}

   (upload-tab)
   (query-tab)
   (library-tab)
   ])

;;;TODO: functionize the html
(hdef/defhtml
 body [status &]
 [:div {:class "widget row"
        :id "interact"}
  (navbar)
  (status-bar)
  [:div {:class "row"}
   (tab-content)
   (sidebar)
   ]]

 )

(defn page-main
  ([status funcs uploads]
   (c/layout (body status)))
  ([status funcs uploads loaded]
   (c/layout (body status))))

(defn file-html [file-list]
  (hic/html
   [:ul (for [e file-list]
          [:li e])]))

(defn func-html [func-list loaded]
  (hic/html
   [:ul (for [e func-list]
          (if (collision e loaded)
            [:strong [:li e]]
            [:li e]))]))

(defn load-html [loaded]
  (hic/html
   (for [e loaded]
     [:div {:class "sidebar-unit"}
      [:ul (for [[k v] e]
             [:li (str k " " v)])]])))
