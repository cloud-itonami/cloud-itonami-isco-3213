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

## License

AGPL-3.0-or-later.
