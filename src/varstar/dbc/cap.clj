;;;; Cap provides entry point for capistrano functionality
;;;;
;;;;     (upload-files files) : nil
;;;;     (update-packages) : nil
;;;;     (install-packages packages) : nil
;;;;     (load-custom-package) : nil
(ns varstar.dbc.cap
  (:use [clojure.java.shell :only [sh]]))

;; Using Capistrano to make sure all nodes get the files.
;; If shadow-mounting is enabled, then this function could be replaced
;; with uploading to one central place
(defn upload-files!
  "Takes a list of filenames and uploads to vertica"
  [filelist target]
  (doall (for [[filename tempfile] filelist]
           (do
             (println "Uploading file" filename "...")
             (prn (sh "cap"
                      "push_lib:upload_file"
                      "-s"
                      (str "filepath=" tempfile)
                      "-s"
                      (str "target=" target)
                      "-s"
                      (str "filename=" filename)))
             ))))

(defn update-packages!
  "Updates packages on R instance for each node"
  []
  (sh "cap" "push_lib:r_update_packages"))

(defn install-packages!
  "Install a list of CRAN packages to R instances"
  [packages]
  (if (< 0 (count packages))
    (try
      (doall (for [p packages]
               (sh "cap"
                   "r_install_package"
                   "-s"
                   (str "package=" p))))
      "Package installed"
      (catch Exception e (.getMessage e)))
    "No packages to install"))

(defn load-custom-package!
  "Push a custom package to R instances"
  [filename]
  (if (> 0 (:exit (sh "cap"
                      "r_install_local_package"
                      "-s" (str "package=uploads/" filename))))
    ""
    (throw (Exception. "Package failed to install"))))
