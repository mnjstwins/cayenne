(ns cayenne.data.doi
  (:import [java.text SimpleDateFormat])
  (:require [cayenne.conf :as conf]
            [cayenne.ids.doi :as doi-id]
            [cayenne.ids.issn :as issn-id]
            [cayenne.ids.isbn :as isbn-id]
            [cayenne.api.query :as query]
            [cayenne.api.response :as r]
            [somnium.congomongo :as m]
            [clj-time.format :as df]
            [clj-time.core :as dt]
            [clj-time.coerce :as dc]
            [clojure.string :as string]))

;; todo eventually produce citeproc from more detailed data stored in mongo
;; for each DOI that comes back from solr. For now, covert the solr fields
;; to some (occasionally ambiguous) citeproc structures.

;; todo API links - orcids, subject ids, doi, issn, isbn, owner prefix

;; todo conneg. currently returning two different formats - item-tree
;; where a DOI is known, citeproc for search results.

(defn ->date-parts
  ([year month day]
     (cond (and year month day)
           {:date-parts [[year, month, day]]}
           (and year month)
           {:date-parts [[year, month]]}
           :else
           {:date-parts [[year]]}))
  ([date-obj]
     (let [d (dc/from-date date-obj)]
       {:date-parts [[(dt/year d) (dt/month d) (dt/day d)]]
        :timestamp (dc/to-long d)})))
        
(defn ->citeproc-contrib [name & orcid]
  (let [base {:literal name}]
    (if orcid
      (assoc base :ORCID orcid)
      base)))

(defn ->citeproc-contribs [solr-doc k]
  (let [contribs (string/split (get solr-doc k) #", ")]
    (map ->citeproc-contrib contribs)))

(defn ->citeproc [solr-doc]
  {:source (get solr-doc "source")
   :volume (get solr-doc "hl_volume")
   :issue (get solr-doc "hl_issue")
   :DOI (doi-id/extract-long-doi (get solr-doc "doi"))
   :URL (get solr-doc "doi")
   :ISBN (map isbn-id/extract-isbn (get solr-doc "isbn"))
   :ISSN (map issn-id/extract-issn (get solr-doc "issn"))
   :title (get solr-doc "hl_title")
   :container-title (get solr-doc "hl_publication")
   :issued (->date-parts (get solr-doc "year") 
                         (get solr-doc "month") 
                         (get solr-doc "day"))
   :deposited (->date-parts (get solr-doc "deposited_at"))
   :indexed (->date-parts (get solr-doc "indexed_at"))
   :author (->citeproc-contribs solr-doc "hl_authors")
   :editor (->citeproc-contribs solr-doc "hl_editors")
   :chair (->citeproc-contribs solr-doc "hl_chairs")
   :contributor (->citeproc-contribs solr-doc "hl_contributors")
   :translator (->citeproc-contribs solr-doc "hl_translators")
   :page (str (get solr-doc "hl_first_page")
              "-" 
              (get solr-doc "hl_last_page"))
   :type (get solr-doc "type")
   :subject (get solr-doc "category")
   :score (get solr-doc "score")})

(defn fetch-dois [query-context]
  (let [doc-list (-> (conf/get-service :solr)
                     (.query (query/->solr-query query-context))
                     (.getResults))]
    (-> (r/api-response :doi-result-list)
        (r/with-result-items (.getNumFound doc-list) (map ->citeproc doc-list))
        (r/with-query-context-info query-context))))

(defn fetch-doi 
  "Fetch a known DOI, which we take from mongo."
  [doi-uri]
  (m/with-mongo (conf/get-service :mongo)
    (m/fetch-one "items" :where {:id doi-uri})))

(defn fetch-random-dois [count]
  (m/with-mongo (conf/get-service :mongo)
    (let [c (or (try (Integer/parseInt count) (catch Exception e nil)) 50)
          records (m/fetch "dois"
                           :where {:random_index {"$gte" (rand)}}
                           :limit c
                           :sort {:random_index 1})]
      (r/api-response :random-doi-list :content (map :doi records)))))
