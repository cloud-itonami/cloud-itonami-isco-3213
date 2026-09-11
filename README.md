# cloud-itonami-isco-3213

Open Occupation Blueprint for **ISCO-08 3213**: Pharmaceutical Technicians and Assistants.

This repository designs a forkable OSS business for an independent pharmaceutical technician: a dispensing-support robot performs counting, labeling and inventory tasks under a governor-gated actor, so the practice keeps its own dispensing and inventory records instead of renting a closed pharmacy-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a dispensing-support robot performs pill counting, labeling and inventory tasks under pharmacist supervision under an actor that proposes
actions and an independent **Pharmacy Support Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
dispensing controlled substances, or dosage verification) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
prescription + verification protocol + inventory record
        |
        v
Dispensing Advisor -> Pharmacy Support Governor -> dispense-support, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `3213`). Required capabilities:

- :robotics
- :identity
- :forms
- :dmn
- :bpmn
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Reference implementation

`src/pharmacy_support/{store,governor}.cljc` is a minimal but real
implementation of the Core Contract above (pure cljc, no external deps):

- `pharmacy-support.store` — `Store` protocol + `MemStore`: registered
  patients (with a known-allergies set), prescription-fills, counseling
  notes. A fill/counseling-note can only be recorded against a
  registered patient (patient provenance).
- `pharmacy-support.governor` — `PharmacySupportGovernor`: `assess` gates
  a proposal against the patient env. Hard invariants force `:hold` (no
  patient, direct-write instead of `:propose`, or a fill whose drug
  matches the patient's known allergy below `:high` safety-class); an
  allergy-conflict fill always requires `:high`+ safety-class and thus
  `:human-approval` — it can never be auto-approved; low-confidence
  proposals also escalate.

```bash
kbb -M:test   # 7 tests, 12 assertions, green
```

This is what backs this repo's `:maturity :implemented` entry in
[`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation) —
the 18th `cloud-itonami-isco-*` occupation to reach that tier, after
`cloud-itonami-isco-6112`, `-2221`, `-7126`, `-4321`, `-9312`, `-5322`,
`-8332`, `-1321`, `-3253`, `-6210`, `-5223`, `-7231`, `-8121`, `-9111`,
`-2512`, `-1120` and `-4110` (ADR-2607012000).

## License

AGPL-3.0-or-later.
