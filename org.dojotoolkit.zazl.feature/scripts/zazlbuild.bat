@echo off

apache-ant-1.8.2\bin\ant -lib org.ant4eclipse_1.0.0.M4\org.ant4eclipse_1.0.0.M4.jar -lib org.ant4eclipse_1.0.0.M4\libs -Dbasedir=%CD% -f ..\zazl\org.dojotoolkit.zazl.feature\scripts\zazlbuild.xml


