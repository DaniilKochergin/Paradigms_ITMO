(use '[clojure.string :only (join)])
(definterface OperationInterface
  (evalJava [args]) (toStringJava []) (diffJava [name]))

(deftype ConstantObj
  [val]
  OperationInterface
  (evalJava [this _] (.val this))
  (toStringJava [this] (format "%.1f" (double (.val this))))
  (diffJava [_ _] (ConstantObj. 0.0)))

(def CONST_ZERO (ConstantObj. 0))
(def CONST_ONE (ConstantObj. 1))
(def CONST_TWO (ConstantObj. 2))

(deftype VariableObj
  [val]
  OperationInterface
  (evalJava [this args] (args (.val this)))
  (toStringJava [this] (.val this))
  (diffJava [this name] (if (= name (.val this)) CONST_ONE CONST_ZERO)))

(deftype OperationFactory
  [operands symbol f der]
  OperationInterface
  (evalJava [this args] (apply f (map #(.evalJava % args) (.operands this))))
  (toStringJava [this] (str "(" (join " " (cons symbol (map #(.toStringJava %) (.operands this)))) ")"))
  (diffJava [this name] (second (apply der (map list (.operands this) (map #(.diffJava % name) (.operands this)))))))

(defn diffOp [f] #(reduce f %&))

(def Add (makeOp ))
(defn Add [& operands]
  (OperationFactory. operands '+ + (diffOp (fn [[x x_der] [y y_der]]
                                             [(Add x y) (Add x_der y_der)]))))

(defn Subtract [& operands]
  (OperationFactory. operands '- - (fn [[x x_der] [y y_der]]
                                     (list (Subtract x y) (Subtract x_der y_der)))))

(defn Multiply [& operands]
  (OperationFactory. operands '* * (fn [[x x_der] [y y_der]]
                                     (list (Multiply x y)
                                           (Add
                                             (Multiply y x_der) (Multiply x y_der))))))

(def smart-div
  (partial reduce (fn [x y] (/ (double x) (double y)))))

(defn Constant [a] (ConstantObj. a))

(defn Variable [a] (VariableObj. a))

(defn Divide [& operands]
  (OperationFactory. operands '/ smart-div (fn [[x x_der] [y y_der]]
                                             (list (Divide x y)
                                                   (Divide
                                                     (Subtract
                                                       (Multiply y x_der) (Multiply x y_der))
                                                     (Multiply y y))))))

(defn Negate [operand]
  (OperationFactory. (list operand) 'negate - (fn [[x x_der]] (list (Negate x)
                                                                    (Negate x_der)))))


(defn Square [operand]
  (OperationFactory. (list operand) 'square (fn [x] (* x x)) (fn [[x x_der]] (list (Square x)
                                                                                   (Multiply (Multiply CONST_TWO x) x_der)))))


(defn Sqrt [operand]
  (OperationFactory. (list operand) 'sqrt (fn [x] (Math/sqrt (max x (- x)))) (fn [[x x_der]] (list (Sqrt (Sqrt (Square x)))
                                                                                                   (Multiply (Divide (Sqrt (Sqrt (Square x)))
                                                                                                                     (Multiply CONST_TWO x)) x_der)))))


(defn evaluate [this vars] (.evalJava this vars))

(defn toString [this] (.toStringJava this))

(defn diff [this var] (.diffJava this var))

(def operations {'+ Add '- Subtract '* Multiply '/ Divide 'negate Negate 'sqrt Sqrt 'square Square})


(defn parse [expr]
  (cond (list? expr) (apply (operations (first expr))
                            (map parse (rest expr)))
        (number? expr) (Constant expr)
        :else (Variable (str expr))))

(defn parseObject [expr]
  (parse (read-string expr)))




(defn parseInfix [expr]
  (with-local-vars [takeOperands (fn []) takeOperandsWithHighPriority (fn []) parseExpr (fn [])
                    oper {"*" 2 "/" 2 "-" 1 "+" 1 "negate" 3 "open" 0 "close" 0 "square" 3 "sqrt" 3}
                    countArgs {"+" 2 "*" 2 "/" 2 "-" 2 "negate" 1 "sqrt" 1 "square" 1}]

    (var-set takeOperands
             (fn [expr numbers op] (if (= "open" (first op))
                                     (parseExpr expr numbers (rest op))
                                     (takeOperands expr
                                                   (cons (apply (operations (read-string (first op)))
                                                                (rseq (apply vector (take (countArgs (first op)) numbers))))
                                                         (drop (countArgs (first op)) numbers))
                                                   (rest op)))))

    (var-set takeOperandsWithHighPriority
             (fn [expr numbers op opToAdd]
               (if (>= (oper (first op)) (oper opToAdd))
                 (takeOperandsWithHighPriority expr
                                               (cons (apply (operations (read-string (first op))) (take (countArgs (first op)) numbers))
                                                     (drop (countArgs (first op)) numbers))
                                               (rest op) opToAdd)
                 (parseExpr expr numbers (cons opToAdd op)))))

    (var-set parseExpr
             (fn [expr numbers op]
               (if (empty? expr) (first numbers)
                                 (cond
                                   (= "open" (first expr)) (parseExpr (rest expr) numbers (cons (first expr) op))
                                   (= "close" (first expr)) (takeOperands (rest expr) numbers op)
                                   (number? (read-string (first expr))) (parseExpr (rest expr) (cons (Constant (read-string (first expr))) numbers) op)
                                   (nil? (find operations (read-string (first expr)))) (parseExpr (rest expr) (cons (Variable (first expr)) numbers) op)
                                   :else (takeOperandsWithHighPriority (rest expr) numbers op (first expr))))))
    (parseExpr (clojure.string/split
                 (str "open "
                      (clojure.string/replace
                        (clojure.string/replace (apply str expr) "(" " open ") ")" " close ")
                      " close")
                 #"\s+")
               (list) (list))
    )
  )

(defn parseObjectInfix [str] (parseInfix [str]))
