(set-env!
 :dependencies '[[adzerk/bootlaces   "0.1.11" :scope "test"]
                 [cljsjs/boot-cljsjs "0.5.0" :scope "test"]])

(require '[boot.core :as core]
         '[boot.tmpdir :as tmpd]
         '[adzerk.bootlaces :refer :all]
         '[cljsjs.boot-cljsjs.packaging :refer :all])

(def +version+ "1.0.4-1")

(task-options!
 pom {:project     'cljsjs/nodejs-externs
      :version     +version+
      :description "Node.js Google Closure Compiler Externs"
      :url         "https://github.com/dcodeIO/node.js-closure-compiler-externs"
      :scm         {:url "https://github.com/cljsjs/packages"}
      :license     {"Apache License" "http://www.apache.org/licenses/LICENSE-2.0.html"}})

(deftask download-nodejs-externs []
  (download :url "https://github.com/dcodeIO/node.js-closure-compiler-externs/archive/1.0.4.zip"
            :checksum "857bf4b4600c87a553293e7cc2dc7cea"
            :unzip true))

(deftask generate-local-deps []
  (let [tmp (core/tmp-dir!)
        deps-file (clojure.java.io/file tmp "deps.cljs")]
    (with-pre-wrap fileset
      (let [extern-files (->> fileset core/input-files (core/by-re [#"cljsjs/nodejs-externs/common/.*.js"]))
            deps-edn {:externs (mapv tmpd/path extern-files)}]
        (spit deps-file (pr-str deps-edn))
        (-> fileset (core/add-resource tmp) core/commit!)))))

(deftask package []
  (comp
   (download-nodejs-externs)
   (sift :move {#"^node.js-closure-compiler-externs-[^/]*/([^/]*).js$"
                "cljsjs/nodejs-externs/common/$1.js"})
   (sift :include #{#"^cljsjs"})
   (generate-local-deps)))
