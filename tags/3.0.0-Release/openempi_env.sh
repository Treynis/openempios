export JAVA_HOME=/sysnet/tools/jdk1.7.0_75
export JAVA_ROOT=${JAVA_HOME}
export JDK_HOME=${JAVA_HOME}
export SDK_HOME=${JAVA_HOME}
export JAVA_BINDIR=${JAVA_HOME}/bin
export JRE_HOME=${JAVA_HOME}/jre
export ANT_HOME=/usr/share/ant/
export MAVEN_HOME=/sysnet/tools/apache-maven-2.2.1
export JBOSS_HOME=/sysnet/servers/jboss-4.2.3.GA
export TOMCAT_HOME=/sysnet/servers/apache-tomcat-7.0.55
export PATH=".:${MAVEN_HOME}/bin:${JAVA_BINDIR}:${ANT_HOME}/bin:$PATH"

echo -----------------------------------
echo    OpenEMPI
echo -----------------------------------

export OPENEMPI_HOME=/sysnet/projects/openempi-os-entity/openempi

export MAVEN_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -agentlib:jdwp=transport=dt_socket,address=8001,server=y,suspend=n -Xms128m -Xmx2048m -XX:MaxPermSize=512m -Dopenempi.home=$OPENEMPI_HOME"

echo -----------------------------------------------
echo  Deploying the application to the Tomcat server
echo -----------------------------------------------
cp $OPENEMPI_HOME/conf/apache-tomcat-setenv.sh $TOMCAT_HOME/bin/setenv.sh
cp $OPENEMPI_HOME/conf/openempi-admin.xml $TOMCAT_HOME/conf/Catalina/localhost

export PGPASSWORD=openempi
dbpresent=`psql --dbname=postgres --host=localhost --username=openempi --command="select datname from pg_database" | grep "openempi-entity$"`

if [ -z $dbpresent ]
then
	echo "Database is already set to openempi-entity"
else
	echo "Database is set to openempi-empi"
	echo "Renaming openempi to openempi-empi"
	psql --dbname=postgres --host=localhost --username=openempi --command='ALTER DATABASE openempi RENAME TO "openempi-empi"' > /dev/null
	echo "Renaming openempi-entity to openempi"
	psql --dbname=postgres --host=localhost --username=openempi --command='ALTER DATABASE "openempi-entity" RENAME TO openempi' > /dev/null
fi
