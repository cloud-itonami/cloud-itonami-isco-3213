(ns pharmacy-support.governor
  "PharmacySupportGovernor — the independent safety/traceability layer
  for the ISCO-08 3213 independent pharmacy-support actor. The Fill
  Advisor proposes actions (prescription-fill, counseling-note); it has
  no notion of patient provenance or allergy-conflict risk, so this MUST
  be a separate system able to *reject* a proposal and fall back to HOLD
  — the itonami-actor pattern (independent Governor gates a proposing
  actor) applied to this occupation.

  Charter (mirrors ADR-2607011000 robotics premise + ADR-2607012000
  cloud-itonami-isco): the actor never dispatches a robot action or writes an
  operating record the governor refuses. A prescription-fill whose drug
  matches a patient's known allergy ALWAYS requires human sign-off — it
  can never be auto-approved.

  HARD invariants for :pharmacy-support/propose:
    1. Patient provenance   — a prescription-fill or counseling-note must
       reference a registered patient.
    2. No-actuation         — the proposal must not directly mutate a
       prescription-fill/counseling-note record outside the
       record-fill!/record-counseling-note! path (effect must be
       :propose, never a raw store write).
    3. Allergy-conflict safety — a prescription-fill whose `drug` is in
       the patient's known `allergies` always requires :high or higher
       safety-class, forcing human sign-off; it is never auto-approved
       regardless of confidence.
  SOFT:
    4. Confidence floor → escalate."
  (:require [pharmacy-support.store :as store]))

(def confidence-floor 0.6)
(def safety-classes [:none :low :medium :high :safety-critical])

(defn- safety-rank [safety-class]
  (let [idx (.indexOf safety-classes safety-class)]
    (if (neg? idx) 0 idx)))

(defn- allergy-conflict? [found-patient proposal]
  (and (= :prescription-fill (:kind proposal))
       found-patient
       (contains? (or (:allergies found-patient) #{}) (:drug proposal))))

(defn- hard-violations [{:keys [patient-fn]} proposal]
  (let [{:keys [patient-id safety-class effect]} proposal
        found-patient (patient-fn patient-id)]
    (cond-> []
      (nil? found-patient)
      (conj {:rule :no-patient :detail (str "未登録 patient " patient-id)})

      (not= :propose effect)
      (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})

      (and (allergy-conflict? found-patient proposal)
           (< (safety-rank (or safety-class :none)) (safety-rank :high)))
      (conj {:rule :allergy-conflict-safety
             :detail "既知アレルギーと一致する drug の fill は :high 以上の safety-class が必須"}))))

(defn assess
  "Assess a proposal against `env` (a map with `:patient-fn` lookup,
  decoupled from any concrete Store so this stays pure). Returns
  `{:decision :proceed|:hold|:human-approval :violations [...] :confidence n}`."
  [env proposal]
  (let [violations (hard-violations env proposal)
        safety-class (or (:safety-class proposal) :none)
        confidence (or (:confidence proposal) 1.0)]
    (cond
      (seq violations)
      {:decision :hold :violations violations :confidence confidence}

      (>= (safety-rank safety-class) (safety-rank :high))
      {:decision :human-approval :violations [] :confidence confidence}

      (< confidence confidence-floor)
      {:decision :human-approval :violations [] :confidence confidence
       :reason :low-confidence}

      :else
      {:decision :proceed :violations [] :confidence confidence})))

(defn env-for-store
  "Build the decoupled env map `assess` needs from a concrete
  `pharmacy-support.store/Store` implementation."
  [store]
  {:patient-fn #(store/patient store %)})
