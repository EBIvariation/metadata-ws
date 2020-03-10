# Handling missing and suppressed entities during import

Some studies present in the import list, or their child entities (such as analyses and samples), may be suppressed or not found in the ENA. Those cases are not distinguishable from the API call result (both return a 404 Not Found error) and are handled in the same way.

Namely, the affected entities will be **skipped** during import with **no exception** being raised. In case an entity (such as a study) is available, but some or all of its child entities (such as analyses) are not available, the transaction **will not** be aborted, and all available entities will still be stored.

For example, the following ENA structure:
* Study α
  + Analysis A
    - Sample 1
    - Sample 2
    - Sample 3 (suppressed)
  + Analysis B (suppressed)
    - Sample 4
    - Sample 5
* Study β (suppressed)
  + Analysis C
    - Sample 6
    - Sample 7

Will lead to the following structure after import:
* Study α
  + Analysis A
    - Sample 1
    - Sample 2

The skipped entities are being logged with a `WARN` type and a specitic error message, for example:
```
2020-03-06 11:11:25.031  WARN 29784 --- [           main] u.a.e.a.m.importer.ObjectsImporter       : Accession not found in ENA ERS3377874
```
