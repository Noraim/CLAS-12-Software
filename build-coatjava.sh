#!/bin/bash

runSpotBugs="yes"
downloadMaps="yes"
for xx in $@
do
    if [ "$xx" == "--nospotbugs" ]
    then
        runSpotBugs="no"
    elif [ "$xx" == "-n" ]
    then
        runSpotBugs="no"
    elif [ "$xx" == "--nomaps" ]
    then
        downloadMaps="no"
    fi
done

if [ $downloadMaps == "yes" ]; then
  wget http://github.com/JeffersonLab/clas12-offline-resources/archive/field-maps.zip
  unzip field-maps.zip
  mkdir etc/data/magfield common-tools/cnuphys/coatjava/etc/data/magfield common-tools/cnuphys/magfieldC/data
  mv clas12-offline-resources-field-maps/etc/* etc/data/magfield/
  mv clas12-offline-resources-field-maps/cnuphys/* common-tools/cnuphys/coatjava/etc/data/magfield/
  mv clas12-offline-resources-field-maps/cnuphysC/* common-tools/cnuphys/magfieldC/data/
  rm -rf field-maps.zip clas12-offline-resources-field-maps
fi

rm -rf coatjava
mkdir -p coatjava
cp -r bin coatjava/
cp -r etc coatjava/
mkdir -p coatjava/lib/clas
cp external-dependencies/JEventViewer-1.1.jar coatjava/lib/clas/
cp external-dependencies/vecmath-1.3.1-2.jar coatjava/lib/clas/
mkdir -p coatjava/lib/utils
cp external-dependencies/jclara-4.3-SNAPSHOT.jar coatjava/lib/utils
cp external-dependencies/KPP-Monitoring-1.0.jar coatjava/lib/utils
cp external-dependencies/KPP-Plots-1.0.jar coatjava/lib/utils
cp external-dependencies/jaw-1.0.jar coatjava/lib/utils
mkdir -p coatjava/lib/services

### clean up any cache copies ###
rm -rf ~/.m2/repository/org/hep/hipo
rm -rf ~/.m2/repository/org/jlab

mvn install # also runs unit tests
if [ $? != 0 ] ; then echo "mvn install failure" ; exit 1 ; fi

if [ $runSpotBugs == "yes" ]; then
	mvn com.github.spotbugs:spotbugs-maven-plugin:spotbugs # spotbugs goal produces a report target/spotbugsXml.xml for each module
	# mvn com.github.spotbugs:spotbugs-maven-plugin:check # check goal produces a report and produces build failed if bugs
	# the spotbugsXml.xml file is easiest read in a web browser
	# see http://spotbugs.readthedocs.io/en/latest/maven.html and https://spotbugs.github.io/spotbugs-maven-plugin/index.html for more info
	if [ $? != 0 ] ; then echo "spotbugs failure" ; exit 1 ; fi
fi

cd common-tools/coat-lib
mvn package
if [ $? != 0 ] ; then echo "mvn package failure" ; exit 1 ; fi
cd -

cp common-tools/coat-lib/target/coat-libs-5.1-SNAPSHOT.jar coatjava/lib/clas/
cp reconstruction/dc/target/clas12detector-dc-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/tof/target/tof-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/cvt/target/cvt-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/ft/target/clas12detector-ft-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/ec/target/clas12detector-ec-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/ltcc/target/clasrec-ltcc-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/htcc/target/clasrec-htcc-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/cnd/target/clas12detector-cnd-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/rich/target/clas12detector-rich-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/fvt/target/clas12detector-fvt-1.0-SNAPSHOT.jar coatjava/lib/services/
cp reconstruction/eb/target/clas12detector-eb-1.0-SNAPSHOT.jar coatjava/lib/services/

echo "COATJAVA SUCCESSFULLY BUILT !"
