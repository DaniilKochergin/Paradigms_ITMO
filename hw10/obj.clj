(use '[clojure.string :only (join)])
(definterface OperationInterface
  (evalJava [args]) (toStringJava []) (diffJava [name]) (toInfixJava []))

(deftype ConstantObj
  [val]
  OperationInterface
  (evalJava [this _] (.val this))
  (toStringJava [this] (format "%.1f" (double (.val this))))
  (diffJava [_ _] (ConstantObj. 0.0))
  (toInfixJava [this] (.toStringJava this)))

(def CONST_ZERO (ConstantObj. 0))
(def CONST_ONE (ConstantObj. 1))
(def CONST_TWO (ConstantObj. 2))

(deftype VariableObj
  [val]
  OperationInterface
  (evalJava [this args] (args (.val this)))
  (toStringJava [this] (.val this))
  (diffJava [this name] (if (= name (.val this)) CONST_ONE CONST_ZERO))
  (toInfixJava [this] (.toStringJava this)))

(def Constant #(ConstantObj. %))
(def Variable #(VariableObj. %))

(deftype OperationFactory
  [operands symbol f der]
  OperationInterface
  (evalJava [this args] (apply f (map #(.evalJava % args) (.operands this))))
  (toStringJava [this] (str "(" (join " " (cons symbol (map #(.toStringJava %) (.operands this)))) ")"))
  (diffJava [this name] (second (apply der (map list (.operands this) (map #(.diffJava % name) (.operands this))))))
  (toInfixJava [this] (if (> (count (.operands this)) 1) (str "(" (join (str " " symbol " ") (map #(.toInfixJava %) (.operands this))) ")")
                                                               (str symbol "(" (.toInfixJava (first operands)) ")"))))


(def toStringInfix #(.toInfixJava %))

(defn diffOp [f] #(reduce f %&))

(defn operation [symbol f der]
  #(OperationFactory. %& symbol f (diffOp der)))

(def Add (operation '+ + (fn [[x x_der] [y y_der]]
                           [(Add x y) (Add x_der y_der)])))


(def Subtract (operation '- - (fn [[x x_der] [y y_der]]
                                [(Subtract x y) (Subtract x_der y_der)])))

(def Multiply (operation '* * (fn [[x x_der] [y y_der]]
                                [(Multiply x y)
                                 (Add
                                   (Multiply y x_der) (Multiply x y_der))])))

(defn any-args [f] #(reduce (fn [x y] (f x y)) %&))

(def Divide (operation '/ (any-args #(/ (double %1) (double %2)))
                       (fn [[x x_der] [y y_der]]
                                      [(Divide x y)
                                       (Divide
                                         (Subtract
                                           (Multiply y x_der) (Multiply x y_der))
                                         (Multiply y y))])))

(defn unaryOperation [symbol f der]
  #(OperationFactory. [%] symbol f der))


(def Negate (unaryOperation 'negate - (fn [[x x_der]] [(Negate x)
                                                       (Negate x_der)])))

(def Square (unaryOperation 'square (fn [x] (* x x)) (fn [[x x_der]] [(Square x)
                                                                      (Multiply (Multiply CONST_TWO x) x_der)])))

(def Sqrt (unaryOperation 'sqrt (fn [x] (Math/sqrt (max x (- x)))) (fn [[x x_der]] [(Sqrt (Sqrt (Square x)))
                                                                                    (Multiply (Divide (Sqrt (Sqrt (Square x)))
                                                                                                      (Multiply CONST_TWO x)) x_der)])))

(defn log-two [a b] (/ (Math/log (Math/abs b))
                       (Math/log (Math/abs a))))

(def Pow
  (operation  '**
              (any-args #(Math/pow (double %1) (double %2))) []))

(def Log
  (operation (symbol "//") (any-args log-two)
             []))


(def evaluate #(.evalJava %1 %2))

(def toString #(.toStringJava %))

(def diff #(.diffJava %1 %2))

(def operations {'+ Add '- Subtract '* Multiply '/ Divide
                 'negate Negate 'sqrt Sqrt 'square Square
                 (symbol "//") Log '** Pow})





