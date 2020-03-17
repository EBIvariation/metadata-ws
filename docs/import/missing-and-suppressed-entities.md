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

# Handling exceptions

During the earlier stages of development, import of Study and Analysis entities was defined to be `@Transactional`, meaning that if any error occurs during import, all changes to the database would be rolled back. This would guarantee a clean but possibly incomplete state after import, where studies and analyses would either be imported completely, or not imported at all.

However, test runs demonstrated that doing a transactional import decreases performance by at least a factor of 10, which is critical. For EVA use case, since the goal is to import all data for all studies, even a single unhandled exception during import means that the import as a whole was a failure and hence the results are unusable in production. Hence, there is no reason to do a transactional import anyway.