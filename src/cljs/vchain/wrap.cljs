(ns vchain.wrap
  (:use [clojure.string :only [split join]]))

; Code snarfed from https://github.com/scpike/word-wrapping/blob/master/clojure/src/wrap/minimum_raggedness.clj

(def default-width 40)

(defn paragraphs
  "Splits an input text into an array of paragraphs"
  [text]
  (split text #"\n\n+"))

(defn words
  "Splits an input text into an array of words"
  [text]
  (split text #"\s+"))

(defn lines-to-text
  "Convert this 2d array (array of array of words) into an array of strings"
  [xs]
  (map #(join " " %) xs))

(defn wrap-paragraph
  "Wrap an individual paragraph using f."
  [f width par]
  (join "\n" (lines-to-text (f width (words par)))))

(defn wrap-paragraphs
  "Wrap all paragraphs in text with f. Return a re-combined string"
  [f width text]
  (join "\n\n" (map #(wrap-paragraph f width %) (paragraphs text))))

(defn wrap-with
  ([f text] (wrap-with f default-width text))
  ([f text width] (wrap-paragraphs f text width)))

(def infinity 100000)

(defn sum-len-between [xs i j]
  "Sum the lengths of the words in xs between index i and j"
  (let [xs (map count (subvec xs i (+ 1 j)))]
    (reduce + xs)))

(defn calc-cost-between
  "Calculate the cost of a line starting at words[i] and ending at
  words[j]. Cost is defined as the square of the whitespace at the
  end of the line. 0 cost for the last line."
  [cutoff i j words]
  (let [cost (- cutoff ; line width
                (- j i) ; spaces
                (sum-len-between words i j))] ; word width
    (if (>= cost 0)
      (if (= (inc j) (count words)) 0 (* cost cost))
      infinity)))
(def cost-between (memoize calc-cost-between))

(declare optimal-cost)
(defn split-cost [cutoff end words split]
  (let [o (optimal-cost cutoff split words)
        c (cost-between cutoff (inc split) end words)]
    [(+ c (first o)) (last o) split]))

(defn best-split [cutoff end words splits]
  (first (sort (map #(split-cost cutoff end words %) splits))))

(defn calc-optimal-cost [cutoff j words]
  (let [cost (cost-between cutoff 0 j words)]
    (if (and (= cost infinity) (> j 0))
      (let [r (best-split cutoff j words (range 0 j))]
        [(first r) (conj (second r) (last r))])
      [cost []])))
(def optimal-cost (memoize calc-optimal-cost))

(defn reconstruct [splits xs]
  (map (fn [x] (subvec xs (first x) (last x)))
       (partition 2 1 (concat [0] (map inc splits) [(count xs)]))))

(defn split-lines [cutoff xs]
  "Split the input based on minimum ragednesss algorithm"
  (let* [ans (optimal-cost cutoff (dec (count xs)) xs)
         splits (last ans)
         cost (first ans)]
    (reconstruct splits xs)))

(defn wrap-text
  ([text] (wrap-with split-lines text))
  ([width text] (wrap-with split-lines width text)))


