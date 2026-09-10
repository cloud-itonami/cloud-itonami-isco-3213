(ns pharmacy-support.store
  "SSoT for the ISCO-08 3213 independent pharmacy-support sole-proprietor
  actor, behind a `Store` protocol so the backend is a swap (MemStore
  default ‖ a real Datomic/kotoba-server backend, per the itonami actor
  pattern).

  Domain = independent pharmacy support practice:

    patient           — a registered patient (patientId, allergies —
                        a set of drug names)
    prescription-fill — a fill event under a patient (fillId, patientId,
                        drug, quantity)
    counseling-note   — a counseling note under a patient (noteId,
                        patientId, content)

  The append-only records are the operating ledger: a fill or counseling
  note must reference a registered patient, and these records are never
  mutated in place, only appended.")

(defprotocol Store
  (patient [st patient-id])
  (fills-of [st patient-id])
  (counseling-notes-of [st patient-id])
  (register-patient! [st patient])
  (record-fill! [st fill])
  (record-counseling-note! [st counseling-note]))

(defrecord MemStore [state]
  Store
  (patient [_ patient-id]
    (get-in @state [:patients patient-id]))
  (fills-of [_ patient-id]
    (filter #(= patient-id (:patient-id %)) (:fills @state)))
  (counseling-notes-of [_ patient-id]
    (filter #(= patient-id (:patient-id %)) (:counseling-notes @state)))
  (register-patient! [_ patient]
    (swap! state assoc-in [:patients (:patient-id patient)] patient))
  (record-fill! [_ fill]
    (swap! state update :fills (fnil conj []) fill))
  (record-counseling-note! [_ counseling-note]
    (swap! state update :counseling-notes (fnil conj []) counseling-note)))

(defn mem-store
  ([] (mem-store {}))
  ([seed]
   (->MemStore (atom (merge {:patients {} :fills [] :counseling-notes []} seed)))))
