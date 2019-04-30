(defn check-vector [vector]
  (and (vector? vector)
       (every? number? vector)))

(defn matrix? [matrix]
  (and (vector? matrix)
       (every? check-vector matrix)
       (apply = (mapv count matrix))))

(defn make-empty [obj]
  (if (number? obj)
    [] (if (vector? obj)
         (mapv make-empty obj) obj)))

(defn check [emp]
  (or (empty? emp) (and (vector? emp)
                        (apply = emp)
                        (every? true? (mapv check emp)))))

(defn object? [obj]
  (check (make-empty obj)))

(defn operation [f & objects]
  {:pre [(and (not (empty? objects)) (object? (vec objects)))]}
  (if (number? (first objects))
    (apply f objects)
    (apply mapv (partial operation f) objects)))


(defn vector-operation [f]
  (fn [& vectors]
    {:pre [(every? check-vector vectors)]}
    (apply (partial operation f) vectors)))

(def v+ (vector-operation +))
(def v- (vector-operation -))
(def v* (vector-operation *))

(defn scalar [a b]
  (apply + (v* a b)))


(defn vector-multiply [a b]
  {:pre [(and (check-vector a) (check-vector b)
              (object? (vector a b))
              (== (count a) (count b) 3))]}
  [(- (* (nth a 1) (nth b 2)) (* (nth a 2) (nth b 1)))
   (- (* (nth a 2) (nth b 0)) (* (nth a 0) (nth b 2)))
   (- (* (nth a 0) (nth b 1)) (* (nth a 1) (nth b 0)))])

(defn vect [& vs]
  (reduce vector-multiply vs))


(defn v*s [v & s]
  {:pre [(and (check-vector v) (check-vector (vec s)))]}
  (mapv (partial * (apply * s)) v))


(defn transpose [a]
  {:pre [(matrix? a)]}
  (apply mapv vector a))

(defn matrix-operation [f]
  (fn [& matrixs]
    {:pre [(every? matrix? matrixs)]}
    (apply (partial operation f) matrixs)))

(def m+ (matrix-operation +))
(def m- (matrix-operation -))
(def m* (matrix-operation *))


(defn m*s [m & s]
  {:pre [(and (matrix? m) (check-vector (vec s)))]}
  (mapv #(apply v*s % s) m))

(defn m*v [m v]
  {:pre [(and (matrix? m) (not (empty? v)))]}
  (mapv (partial scalar v) m))

(defn matrix-multiply [a b]
  {:pre [(and (matrix? a) (matrix? b)
              (== (count (a 0)) (count b)))]}
  (transpose (mapv (partial m*v a) (transpose b))))

(defn m*m [& ms]
  {:pre [(not (empty? ms))]}
  (reduce matrix-multiply ms))

(defn count-suf [a b]
  (if (empty? a)
    0
    (if (== (first a) (first b))
      (inc (count-suf (rest a) (rest b)))
      0)))

(defn broadcast? [a b]
  (== (count-suf (reverse a) (reverse b)) (count a)))

(defn form [obj]
  (if (vector? obj)
    (if (vector? (nth obj 0))
      (cons (count obj) (form (nth obj 0)))
      (list (count obj) 1))
    (list 1)))


(defn increase [shape obj]
  (if (empty? shape)
    obj
    (vec (repeat (first shape) (increase (rest shape) obj)))))

(defn broadcast [shape obj]
  (increase (drop-last (count (form obj)) shape) obj))


(defn greatest-tensor [tensors]
  (apply max-key (fn [t] (count (form t))) tensors))

(defn broadcast-tensors [tensors]
  (let [g (form (greatest-tensor tensors))]
    {:pre [every? #((broadcast? (form %) g)) tensors]}
    (mapv (partial broadcast g) tensors)))


(defn tensor-operation [f]
  (fn [& tensors]
    (apply (partial operation f) (broadcast-tensors tensors))))


(def b+ (tensor-operation +))
(def b- (tensor-operation -))
(def b* (tensor-operation *))
