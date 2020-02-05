# Querying the metadata service

## Analysis queries

It is possible to search for multiple technology types, for example, this will return all analyses which were genotyped using either by array or by sequencing:

```
GET /analyses/search?technology=GENOTYPING_BY_ARRAY&technology=GENOTYPING_BY_SEQUENCING
```