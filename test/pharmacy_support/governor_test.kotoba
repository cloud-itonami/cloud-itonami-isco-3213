(ns pharmacy-support.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [pharmacy-support.store :as store]
            [pharmacy-support.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-patient! st {:patient-id "patient-1" :allergies #{"penicillin"}})
    st))

(deftest proceeds-on-clean-fill
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "patient-1" :drug "ibuprofen"
                   :quantity 30 :safety-class :low :effect :propose :confidence 0.9}]
    (is (= :proceed (:decision (governor/assess env proposal))))))

(deftest holds-on-unregistered-patient
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "no-such-patient" :drug "ibuprofen"
                   :quantity 30 :safety-class :low :effect :propose :confidence 0.9}
        result (governor/assess env proposal)]
    (is (= :hold (:decision result)))
    (is (some #(= :no-patient (:rule %)) (:violations result)))))

(deftest holds-on-no-actuation-violation
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "patient-1" :drug "ibuprofen"
                   :quantity 30 :safety-class :low :effect :direct-write :confidence 0.9}
        result (governor/assess env proposal)]
    (is (= :hold (:decision result)))
    (is (some #(= :no-actuation (:rule %)) (:violations result)))))

(deftest holds-on-allergy-conflict-fill-without-high-safety-class
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "patient-1" :drug "penicillin"
                   :quantity 30 :safety-class :medium :effect :propose :confidence 0.9}
        result (governor/assess env proposal)]
    (is (= :hold (:decision result)))
    (is (some #(= :allergy-conflict-safety (:rule %)) (:violations result)))))

(deftest human-approval-on-allergy-conflict-fill-with-high-safety-class
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "patient-1" :drug "penicillin"
                   :quantity 30 :safety-class :high :effect :propose :confidence 0.9}]
    (is (= :human-approval (:decision (governor/assess env proposal))))))

(deftest human-approval-on-low-confidence
  (let [st (fresh-store)
        env (governor/env-for-store st)
        proposal {:kind :prescription-fill :patient-id "patient-1" :drug "ibuprofen"
                   :quantity 30 :safety-class :none :effect :propose :confidence 0.2}
        result (governor/assess env proposal)]
    (is (= :human-approval (:decision result)))
    (is (= :low-confidence (:reason result)))))

(deftest store-records-append-only
  (let [st (fresh-store)]
    (store/record-fill! st {:fill-id "f1" :patient-id "patient-1" :drug "ibuprofen" :quantity 30})
    (store/record-counseling-note! st {:note-id "n1" :patient-id "patient-1" :content "take with food"})
    (is (= 1 (count (store/fills-of st "patient-1"))))
    (is (= 1 (count (store/counseling-notes-of st "patient-1"))))))

(deftest a-proposal-without-confidence-does-not-proceed
  (testing "確信度を言っていない提案は、確信していると言っていないので auto-proceed
            させない。この既定は 2026-07-30 まで 1.0 で、:confidence を持たない提案が
            :proceed していた（ADR-2607309100）。fleet の boolean 方言 346 件はすべて
            0.0 既定で、うち isco-5419 はそれを明示的にテストしている。"
    (let [st (fresh-store)
          env (governor/env-for-store st)
          proposal {:kind :prescription-fill :patient-id "patient-1" :drug "ibuprofen" :quantity 30 :safety-class :low :effect :propose}
          result (governor/assess env proposal)]
      (is (= 0.0 (:confidence result))
          "欠落した :confidence は 0.0 であって 1.0 ではない")
      (is (not= :proceed (:decision result))
          "確信度不明の提案が自動で通ってはならない"))))
