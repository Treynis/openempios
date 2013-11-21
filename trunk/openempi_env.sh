export JAVA_HOME=/sysnet/tools/jdk1.6.0
export JAVA_ROOT=${JAVA_HOME}
export JDK_HOME=${JAVA_HOME}
export SDK_HOME=${JAVA_HOME}
export JAVA_BINDIR=${JAVA_HOME}/bin
export JRE_HOME=${JAVA_HOME}/jre
export ANT_HOME=/usr/share/ant/
export MAVEN_HOME=/sysnet/tools/apache-maven-2.2.1
export JBOSS_HOME=/sysnet/servers/jboss-4.2.3.GA
export PATH=".:${MAVEN_HOME}/bin:${JAVA_BINDIR}:${ANT_HOME}/bin:$PATH"

echo -----------------------------------
echo    OpenEMPI
echo -----------------------------------

export OPENEMPI_HOME=/sysnet/projects/openempi-os-entity/openempi
