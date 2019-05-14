(load-file (.getCanonicalPath (clojure.java.io/file "obj.clj")))
(load-file (.getCanonicalPath (clojure.java.io/file "combinators.clj")))

(declare add-sub)
(def unary
  (delay (+seqn 0 *ws (+or (+seqn 1 (+char "(") add-sub (+char ")"))
                           (+map #(Negate (second %))
                                 (+seq (*operator [] ["negate"]) unary))
                           *number *variable) *ws)))
(defn some-assoc [is-left]
  (fn [a] (let [ra (if is-left a (reverse a))]
            (reduce #(apply (operations (first %2))
                            (if is-left [%1 (second %2)]
                                        [(second %2) %1]))
                    (first ra) (partition 2 (rest ra))))))
(defn abstract [next fakes operators type]
  (+map (some-assoc type) (+seqf cons next
                                 (+map (partial apply concat)
                                       (+star (+seq (*operator fakes operators) next))))))
(def pow-log (abstract unary [] ["**" "//"] false))
(def mul-div (abstract pow-log ["**" "//"] ["*" "/"] true))
(def add-sub (abstract mul-div [] ["+" "-"] true))

(defn parseObjectInfix [exp]
  ((+parser add-sub) exp))