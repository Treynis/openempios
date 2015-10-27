#!/bin/sh
# ----------------------------------------------------------------------------
#  Copyright 2001-2006 The Apache Software Foundation.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
#   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
#   reserved.

JAVA_OPTS="-Dlog.console.level=SEVERE"

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR/.." >/dev/null; pwd`

# Reset the REPO variable. If you need to influence this use the environment setup file.
REPO=$OPENEMPI_HOME/lib


# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_VERSION" ] ; then
             JAVA_VERSION="CurrentJDK"
           else
             echo "Using Java version: $JAVA_VERSION"
           fi
		   if [ -z "$JAVA_HOME" ]; then
		      if [ -x "/usr/libexec/java_home" ]; then
			      JAVA_HOME=`/usr/libexec/java_home`
			  else
			      JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/${JAVA_VERSION}/Home
			  fi
           fi       
           ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# If a specific java binary isn't specified search for the standard 'java' binary
if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java`
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." 1>&2
  echo "  We cannot execute $JAVACMD" 1>&2
  exit 1
fi

if [ -z "$REPO" ]
then
  REPO="$BASEDIR"/lib
fi

CLASSPATH="$OPENEMPI_HOME"/conf:"$REPO"/openempi-entity-configuration-3.1.0.jar:"$REPO"/openempi-entity-single-best-record-3.1.0.jar:"$REPO"/spring-jdbc-3.1.1.RELEASE.jar:"$REPO"/spring-beans-3.1.1.RELEASE.jar:"$REPO"/spring-core-3.1.1.RELEASE.jar:"$REPO"/spring-asm-3.1.1.RELEASE.jar:"$REPO"/commons-logging-1.1.jar:"$REPO"/spring-tx-3.1.1.RELEASE.jar:"$REPO"/aopalliance-1.0.jar:"$REPO"/spring-aop-3.1.1.RELEASE.jar:"$REPO"/spring-context-3.1.1.RELEASE.jar:"$REPO"/spring-expression-3.1.1.RELEASE.jar:"$REPO"/spring-aspects-3.1.1.RELEASE.jar:"$REPO"/spring-context-support-3.1.1.RELEASE.jar:"$REPO"/xmlbeans-2.4.0.jar:"$REPO"/stax-api-1.0.1.jar:"$REPO"/maven-jaxb2-plugin-0.8.1.jar:"$REPO"/maven-jaxb2-plugin-core-0.8.1.jar:"$REPO"/resolver-20050927.jar:"$REPO"/maven-plugin-api-2.0.6.jar:"$REPO"/plexus-build-api-0.0.7.jar:"$REPO"/plexus-utils-1.5.15.jar:"$REPO"/maven-project-2.0.6.jar:"$REPO"/maven-settings-2.0.6.jar:"$REPO"/maven-model-2.0.6.jar:"$REPO"/plexus-container-default-1.0-alpha-9-stable-1.jar:"$REPO"/junit-4.11.jar:"$REPO"/classworlds-1.1-alpha-2.jar:"$REPO"/maven-profile-2.0.6.jar:"$REPO"/maven-artifact-manager-2.0.6.jar:"$REPO"/maven-repository-metadata-2.0.6.jar:"$REPO"/maven-artifact-2.0.6.jar:"$REPO"/wagon-provider-api-1.0-beta-2.jar:"$REPO"/maven-plugin-registry-2.0.6.jar:"$REPO"/maven-plugin-anno-1.3.1.jar:"$REPO"/maven-jaxb22-plugin-0.8.1.jar:"$REPO"/jaxb-impl-2.2.5-b10.jar:"$REPO"/jaxb-xjc-2.2.5-b10.jar:"$REPO"/postgresql-9.3-1103-jdbc3.jar:"$REPO"/commons-lang-2.3.jar:"$REPO"/tomcat-jdbc-7.0.35.jar:"$REPO"/tomcat-juli-7.0.35.jar:"$REPO"/log4j-1.2.15.jar:"$REPO"/mail-1.4.jar:"$REPO"/activation-1.1.jar:"$REPO"/jms-1.1.jar:"$REPO"/jmxtools-1.2.1.jar:"$REPO"/jmxri-1.2.1.jar:"$REPO"/spring-security-core-tiger-2.0.1.jar:"$REPO"/spring-security-core-2.0.1.jar:"$REPO"/commons-codec-1.6.jar:"$REPO"/commons-collections-3.2.jar:"$REPO"/jsr250-api-1.0.jar:"$REPO"/openempi-entity-core-3.1.0.jar:"$REPO"/uuid-3.1.jar:"$REPO"/simmetrics-1.6.2.jar:"$REPO"/commons-beanutils-1.8.0.jar:"$REPO"/commons-pool-1.4.jar:"$REPO"/persistence-api-1.0.jar:"$REPO"/ehcache-core-2.6.2.jar:"$REPO"/slf4j-api-1.6.1.jar:"$REPO"/slf4j-log4j12-1.6.1.jar:"$REPO"/aspectjweaver-1.8.4.jar:"$REPO"/aspectjrt-1.8.4.jar:"$REPO"/xbean-spring-3.7.jar:"$REPO"/dbcp-6.0.33.jar:"$REPO"/orientdb-core-2.0.14.jar:"$REPO"/snappy-java-1.1.0.1.jar:"$REPO"/concurrentlinkedhashmap-lru-1.4.1.jar:"$REPO"/jna-4.0.0.jar:"$REPO"/jna-platform-4.0.0.jar:"$REPO"/orientdb-graphdb-2.0.14.jar:"$REPO"/orientdb-server-2.0.14.jar:"$REPO"/orientdb-client-2.0.14.jar:"$REPO"/orientdb-enterprise-2.0.14.jar:"$REPO"/orientdb-tools-2.0.14.jar:"$REPO"/orientdb-object-2.0.14.jar:"$REPO"/hibernate-jpa-2.0-api-1.0.0.Final.jar:"$REPO"/javassist-3.16.1-GA.jar:"$REPO"/blueprints-core-2.6.0.jar:"$REPO"/jettison-1.3.3.jar:"$REPO"/jackson-databind-2.2.3.jar:"$REPO"/jackson-annotations-2.2.3.jar:"$REPO"/jackson-core-2.2.3.jar:"$REPO"/hppc-0.6.0.jar:"$REPO"/commons-configuration-1.6.jar:"$REPO"/commons-digester-1.7.jar:"$REPO"/commons-beanutils-core-1.8.0.jar:"$REPO"/gremlin-java-2.6.0.jar:"$REPO"/pipes-2.6.0.jar:"$REPO"/gremlin-groovy-2.6.0.jar:"$REPO"/ivy-2.3.0.jar:"$REPO"/groovy-1.8.9.jar:"$REPO"/antlr-2.7.6.jar:"$REPO"/asm-3.2.jar:"$REPO"/asm-commons-3.2.jar:"$REPO"/asm-tree-3.2.jar:"$REPO"/asm-util-3.2.jar:"$REPO"/asm-analysis-3.2.jar:"$REPO"/ant-1.8.3.jar:"$REPO"/ant-launcher-1.8.3.jar:"$REPO"/jansi-1.5.jar:"$REPO"/jline-0.9.94.jar:"$REPO"/snappy-0.3.jar:"$REPO"/hibernate-core-3.3.2.GA.jar:"$REPO"/dom4j-1.6.1.jar:"$REPO"/xml-apis-1.3.02.jar:"$REPO"/jta-1.1.jar:"$REPO"/cglib-nodep-2.1_3.jar:"$REPO"/hibernate-annotations-3.4.0.GA.jar:"$REPO"/ejb3-persistence-1.0.2.GA.jar:"$REPO"/hibernate-commons-annotations-3.1.0.GA.jar:"$REPO"/hibernate-validator-3.1.0.GA.jar:"$REPO"/javassist-3.12.1.GA.jar:"$REPO"/spring-jms-3.1.1.RELEASE.jar:"$REPO"/spring-orm-3.1.1.RELEASE.jar:"$REPO"/velocity-1.4.jar:"$REPO"/velocity-dep-1.4.jar:"$REPO"/opencsv-2.1.jar:"$REPO"/activemq-core-5.4.2.jar:"$REPO"/commons-logging-api-1.1.jar:"$REPO"/geronimo-jms_1.1_spec-1.1.1.jar:"$REPO"/activeio-core-3.1.2.jar:"$REPO"/geronimo-j2ee-management_1.1_spec-1.0.1.jar:"$REPO"/kahadb-5.4.2.jar:"$REPO"/activemq-protobuf-1.1.jar:"$REPO"/org.osgi.core-4.1.0.jar:"$REPO"/commons-net-2.0.jar:"$REPO"/jasypt-1.6.jar:"$REPO"/icu4j-4.0.1.jar:"$REPO"/activemq-pool-5.4.2.jar:"$REPO"/geronimo-jta_1.0.1B_spec-1.0.1.jar:"$REPO"/jasperreports-4.5.0.jar:"$REPO"/jcommon-1.0.15.jar:"$REPO"/jfreechart-1.0.12.jar:"$REPO"/castor-1.2.jar:"$REPO"/poi-ooxml-3.7.jar:"$REPO"/poi-3.7.jar:"$REPO"/poi-ooxml-schemas-3.7.jar:"$REPO"/geronimo-stax-api_1.0_spec-1.0.jar:"$REPO"/jackson-core-lgpl-1.7.4.jar:"$REPO"/jackson-mapper-lgpl-1.7.4.jar:"$REPO"/groovy-all-1.7.5.jar:"$REPO"/jdom-1.1.3.jar:"$REPO"/openempi-entity-basic-blocking-3.1.0.jar:"$REPO"/openempi-entity-basic-blocking-hp-3.1.0.jar:"$REPO"/hazelcast-3.4.2.jar:"$REPO"/annotations-1.3.2.jar:"$REPO"/minimal-json-0.9.1.jar:"$REPO"/openempi-entity-file-loader-hp-3.1.0.jar:"$REPO"/openempi-entity-file-loader-map-3.1.0.jar:"$REPO"/openempi-entity-exact-matching-3.1.0.jar:"$REPO"/openempi-entity-probabilistic-matching-3.1.0.jar:"$REPO"/openempi-entity-shallow-matching-3.1.0.jar:"$REPO"/hamcrest-core-1.3.jar:"$REPO"/jmock-2.4.0.jar:"$REPO"/hamcrest-library-1.1.jar:"$REPO"/jmock-junit4-2.4.0.jar:"$REPO"/junit-dep-4.4.jar:"$REPO"/spring-test-3.1.1.RELEASE.jar

ENDORSED_DIR=
if [ -n "$ENDORSED_DIR" ] ; then
  CLASSPATH=$BASEDIR/$ENDORSED_DIR/*:$CLASSPATH
fi

if [ -n "$CLASSPATH_PREFIX" ] ; then
  CLASSPATH=$CLASSPATH_PREFIX:$CLASSPATH
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$HOME" ] && HOME=`cygpath --path --windows "$HOME"`
  [ -n "$BASEDIR" ] && BASEDIR=`cygpath --path --windows "$BASEDIR"`
  [ -n "$REPO" ] && REPO=`cygpath --path --windows "$REPO"`
fi

exec "$JAVACMD" $JAVA_OPTS -Xms10248m -Dopenempi.home=$OPENEMPI_HOME \
  -classpath "$CLASSPATH" \
  -Dapp.name="fileLoaderManager" \
  -Dapp.pid="$$" \
  -Dapp.repo="$REPO" \
  -Dapp.home="$BASEDIR" \
  -Dbasedir="$BASEDIR" \
  -Dlog.console.level=SEVERE \
  org.openhie.openempi.loader.FileLoaderManager \
  $OPENEMPI_HOME/conf/test-data-5k.csv flexibleDataLoader person true file-loader-map-testing.xml false true "$@"
