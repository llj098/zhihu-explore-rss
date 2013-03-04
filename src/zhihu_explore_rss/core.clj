(ns zhihu-explore-rss.core
  (:require [net.cgrand.enlive-html :as html])
  (:require [clj-rss.core :as rss])
  (:gen-class))

(use 'clojure.java.io)

(def *base-url* "http://www.zhihu.com/explore")
(def *title-selector* [:.zm-item-title :> :a])
(def *content-selector* [:div :> :textarea])
(def *author-selector* [:.zm-item-answer-author-wrap :> :a])
(def *escape-map* {\< "&lt;", \> "&gt;", \& "&amp;"
                   \" "quot;" \' "apos;"})

(defn html-encode [s]
  (clojure.string/escape s *escape-map*))

(defn parse [item]
  (let [title (first (html/select item *title-selector*))
        author (second (html/select item *author-selector*))
        content (first (html/select item *content-selector*))]
    (hash-map :title (html-encode (first (:content title)))
              :link (str "http://www.zhihu.com/" (:href (:attrs title)))
              :description (html-encode (first (:content content)))
              :author (html-encode (first (:content author))))))

(defn gen-rss [items]
  (apply rss/channel-xml
         {:title "知乎-热门回答" :link "http://www.zhihu.com/read" 
          :description "知乎-热门回答"} items))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))


(defn zh-items []
  (html/select (fetch-url *base-url*) [:div#zh-explore-list :> :div]))

(defn save-file [path content]
  (with-open [wrtr (writer path)]
    (.write wrtr content)))

(defn -main [path]
  (try
    (let [s (gen-rss (map parse (zh-items)))]
      (do 
        (save-file path s)
        (println 1)))
    (catch Exception ex (println ex))))