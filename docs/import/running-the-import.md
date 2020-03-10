# How to run the import pipeline

Configure settings:
```bash
# Repository and branch used for the run. Useful for testing on different branches.
export REPOSITORY=https://github.com/EBIvariation/metadata-ws
export BRANCH=master

# Path where Maven is installed (or will be installed)
# See also instructions below
export MAVEN_INSTALL_PATH=
export PATH=$MAVEN_INSTALL_PATH/bin:$PATH

# Oracle credentials to download the database drivers
# See Confluence → Variation home → IT infrastructure → External services credentials → Oracle
export ORACLE_USERNAME=
export ORACLE_PASSWORD=

# Entrez API key to do batch queries
# See Confluence → Variation home → IT infrastructure → External services credentials → NCBI Entrez API key 
export ENTREZ_API_KEY=

# Source database credentials (ENA)
# See Confluence → Variation home → IT infrastructure → Database Connections → ENA database credentials
export ENA_DATASOURCE_URL=
export ENA_DATASOURCE_USERNAME=
export ENA_DATASOURCE_PASSWORD=

# Target database (Postgres), which will be used to store the import results
# See Confluence → Variation home → IT infrastructure → Database Connections → AMP metadata staging database
export METADATA_DATASOURCE_URL=
export METADATA_DATASOURCE_USERNAME=
export METADATA_DATASOURCE_PASSWORD=
```

If you don't have Maven, install it. Note that the version used on the cluster (3.0.5) has a bug and cannot read `settings.xml` correctly.
```bash
mkdir -p $MAVEN_INSTALL_PATH
cd $MAVEN_INSTALL_PATH
wget -q http://us.mirrors.quenda.co/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
tar --extract --verbose --file=apache-maven-3.6.3-bin.tar.gz
rm apache-maven-3.6.3-bin.tar.gz
mv apache-maven-3.6.3/* .
rmdir apache-maven-3.6.3
```

Clone the repository and build the package:
```bash
git clone $REPOSITORY
cd metadata-ws
git checkout $BRANCH
sed -ie "s|<username></username>|<username>${ORACLE_USERNAME}</username>|" settings.xml
sed -ie "s|<password></password>|<password>${ORACLE_PASSWORD}</password>|" settings.xml
mvn -s settings.xml clean install -DskipTests=true
```

Write the list of studies to import to `studies-to-import.txt`.

Run the import:
```bash
time bsub -I java \
  -jar metadata-load/target/metadata-load-1.0-SNAPSHOT.jar \
  uk.ac.ebi.ampt2d.metadata.importer.MetadataImporterMainApplication \
    --accessions.file.path=studies-to-import.txt \
    --import.source=API \
    --entrez.api.key=$ENTREZ_API_KEY \
    --ena.datasource.url=$ENA_DATASOURCE_URL \
    --ena.datasource.username=$ENA_DATASOURCE_USERNAME \
    --ena.datasource.password=$ENA_DATASOURCE_PASSWORD \
    --metadata.datasource.url=$METADATA_DATASOURCE_URL \
    --metadata.datasource.username=$METADATA_DATASOURCE_USERNAME \
    --metadata.datasource.password=$METADATA_DATASOURCE_PASSWORD \
2>&1 | tee run.log
```
