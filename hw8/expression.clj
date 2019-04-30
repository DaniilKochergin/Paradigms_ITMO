(defn operationFactory [f]
  (fn [& args] (fn [vars] (apply f (mapv #(% vars) args)))))

(def constant constantly)

(defn variable [x] (fn [vars] (vars x)))

(def add (operationFactory +))

(def subtract (operationFactory -))

(def multiply (operationFactory *))

(defn smart-div [& values]
  (reduce (fn [x y] (/ (double x) (double y))) values))

(def divide (operationFactory smart-div))

(def negate (operationFactory -))


(defn any-args [f] (fn [& args] (reduce #(f %1 %2) args)))

(def max (operationFactory (any-args clojure.core/max)))
(def min (operationFactory (any-args clojure.core/min)))


(def operations {'+ add '- subtract '* multiply '/ divide 'negate negate 'max max 'min min})

(defn parse [exp]
  (cond
    (list? exp) (apply
                  (operations (first exp))
                  (map parse (rest exp)))
    (number? exp) (constant exp)
    :else (variable (str exp))))

(def parseFunction (comp parse read-string))