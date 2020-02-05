# Using the metadata service

## Study queries

Please note that a comma symbol (`,`) is not permitted to be used in a study accession, because this symbol is used to separate study accessions when configuring access control.

## Analysis queries

It is possible to search for multiple technology types, for example, this will return all analyses which were genotyped using either by array or by sequencing:

```
GET /analyses/search?technology=GENOTYPING_BY_ARRAY&technology=GENOTYPING_BY_SEQUENCING
```
