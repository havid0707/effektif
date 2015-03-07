#/bin/sh

# downloads and configures a gatling distribution that does not need maven

rm -Rf gatling-dist &&
mkdir gatling-dist &&
cd gatling-dist &&
wget https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/2.1.4/gatling-charts-highcharts-bundle-2.1.4-bundle.zip &&
unzip gatling-charts-highcharts-bundle-2.1.4-bundle.zip &&
mv gatling-charts-highcharts-bundle-2.1.4/* . &&
rmdir gatling-charts-highcharts-bundle-2.1.4 &&
rm gatling-charts-highcharts-bundle-2.1.4-bundle.zip &&
rm -Rf user-files/simulations/* &&
cp -r ../../src/test/scala/* user-files/simulations/ &&
cp /cygdrive/d/jpx/maven-repository/org/apache/httpcomponents/httpcore/4.3.3/httpcore-4.3.3.jar lib/ &&
cp /cygdrive/d/jpx/maven-repository/org/apache/httpcomponents/httpclient/4.3.6/httpclient-4.3.6.jar lib/ &&
cp /cygdrive/d/jpx/maven-repository/joda-time/joda-time/2.5/joda-time-2.5.jar lib/ &&
cp /cygdrive/d/jpx/maven-repository/com/fasterxml/jackson/core/jackson-core/2.4.3/jackson-core-2.4.3.jar lib/ &&
cp ../../../effektif-workflow-api/target/effektif-workflow-api-1.0.0-SNAPSHOT.jar lib/ &&
cp ../../../effektif-workflow-impl/target/effektif-workflow-impl-1.0.0-SNAPSHOT.jar lib/ &&
cd .. &&
echo Done
echo run with bin/gatling.sh -m -s effektif.EffektifSimulation



