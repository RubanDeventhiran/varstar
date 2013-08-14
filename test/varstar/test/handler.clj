(ns varstar.test.handler
  (:use clojure.test
        ring.mock.request
        varstar.handler))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= (:status response) 200))
      (is (= (:body response) "Hello World"))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "shell route"
    (let [response (app (request :get "/shell"))]
      (is (= (:status response) 200)))))
