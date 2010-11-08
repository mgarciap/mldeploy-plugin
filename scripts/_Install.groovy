//
// This script is executed by Grails after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/grails-app/jobs")

config = new File("${basedir}/grails-app/conf/mldeploy.groovy")


println '''
Welcome to Mercadolibre New World deployment tools plugin!
----------------------------------------------------------

To get started you need to set up your environments configutarions:
i.e.: servers, deployment and authentication modes, etc.
You will find a configuration file example in 'grails-app/config/mldeploy.groovy'

----------------------------------------------------------
'''
ant.copy(todir:"${basedir}/lib") {
  fileset(dir:"${pluginBasedir}/lib", includes:"*.*")
}

if(!config.exists()) {
  println "This plugins has never been installed in this application. Let's initialize it..."
  ant.copy(file:"${pluginBasedir}/src/templates/mldeploy.groovy", todir:"${basedir}/grails-app/conf")
  }
else {
     println "This plugins has already been installed. Please check the upgrade guide (wiki)"
  }
