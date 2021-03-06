(ns cayenne.data.license
  (:require [cayenne.api.v1.query :as query]
            [cayenne.api.v1.response :as r]
            [cayenne.api.v1.filter :as filter]
            [cayenne.conf :as conf]))

(defn ->license-doc [facet-field-value]
  {:URL (.getName facet-field-value)
   :work-count (.getCount facet-field-value)})

;; todo why are odd license URLs appearing with 0 counts?

;; todo offset, rows

(defn fetch-all [query-context]
  (let [q (-> query-context
              (assoc :facets [{:field "license" :count -1}])
              (query/->solr-query :filters filter/std-filters))
        facet-field (-> (conf/get-service :solr)
                        (.query q)
                        (.getFacetField "license_url"))
        facet-values (->> (.getValues facet-field)
                          (filter #(not= 0 (.getCount %))))]
    (-> (r/api-response :license-list)
        (r/with-result-items
          (count facet-values)
          (map ->license-doc facet-values))
        (r/with-query-context-info query-context))))
              
