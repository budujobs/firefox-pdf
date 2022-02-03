(ns core
  {:clj-kondo/config '{:lint-as {promesa.core/let clojure.core/let
                                 example/defp clojure.core/def}}}
  (:require
   ["puppeteer$default" :as puppeteer]
   ["express$default" :as express]
   ["fs$default" :as fs]
   [clojure.string :as str]
   [clojure.test :as t :refer [deftest is async]]
   [promesa.core :as p]))

(defn- on-sent
  [res temp-path err]
  (.unlink fs temp-path (fn [_]))
  (when err
    (-> res
      (.status 500)
      (.send "An error occurred."))))

(defn print-pdf [url]
  (p/let [browser (.launch puppeteer #js {:headless true
                                          :product "firefox"})
          page (.newPage browser)
          temp-path (str "/tmp/cv-" (.toString (random-uuid)) ".pdf")
          _ (.goto page url)
          _ (-> (.pdf page #js{:path temp-path
                               :format "A3"
                               #_#_:preferCSSPageSize true
                               #_#_:margin #js{:top "0.5cm"
                                           :bottom "0.5cm"
                                           :left "0.5cm"
                                           :right "0.5cm"}})
                (.catch #(js/console.log %)))]
    (.close browser)
    temp-path))

(def port 8092)
(def app  (doto (express)
            (.get "/"
                  (fn [req res]
                    (p/let [temp-path (print-pdf (.. req -query -url))
                            file-name "cv.pdf"]
                      (.download res temp-path file-name
                                 (partial on-sent res temp-path)))))
            (.listen port (fn [] (println "hello")))))
