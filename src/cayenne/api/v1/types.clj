(ns cayenne.api.v1.types)

(def depositable ["application/vnd.crossref.deposit+xml"
                  "application/vnd.crossref.partial+xml"
                  "application/vnd.crossref.any+xml"
                  "application/pdf"
                  "application/vnd.crossref.patent-citations+csv"
                  "application/vnd.crossref.patent-citations+csv+g-zip"
                  "application/vnd.crossref.patent-citations+tab-separated-values"
                  "application/vnd.crossref.patent-citations+tab-separated-values+g-zip"])

(def html-or-json ["application/json" "text/html"])

(def json ["application/json"])

(def deposit "application/vnd.crossref.deposit+json")

(def prefix "application/vnd.crossref.prefix+json")

(def work-transform
  ["application/rdf+xml"
   "text/turtle"
   "text/n-triples"
   "text/n3"
   "application/vnd.citationstyles.csl+json"
   "application/citeproc+json"
   "application/json"
   "text/x-bibliography"
   "text/bibliography"
   "text/plain"
   "application/x-research-info-systems"
   "application/x-bibtex"
   "application/vnd.crossref.unixref+xml"
   "application/unixref+xml"
   "application/vnd.crossref.unixsd+xml"])

