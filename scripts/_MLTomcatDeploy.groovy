/**
*  Mercadolibre's Ground Zero Deployment Scripts: Tomcat Mode
*     - Uses tomcat default plugin
*  2010-09-27 - Manuel: version inicial
*/

scriptScope=grails.util.BuildScope.WAR
includeTargets << grailsScript("_GrailsWar" )

ant.taskdef(name:"tomcatdeploy",classname:"org.apache.catalina.ant.DeployTask")
ant.taskdef(name:"tomcatlist",classname:"org.apache.catalina.ant.ListTask")
ant.taskdef(name:"tomcatundeploy",classname:"org.apache.catalina.ant.UndeployTask")

scriptEnv = Ant.project.properties.'grails.env'

//All environment Tomcat server Configurations
def serversConfig = [:]

// @DEPRECATED
// Single server name or ALL servers
//def targetServers = []

parseTomcatServer = {serverName, server ->
    def targetLabel = "[DEBUG - Parse Tomcat Server: $serverName]"
	  println "$targetLabel Parameters [serverName: $serverName, server: $server]"   
    def usr
    def pass
		def useExternalCredentials = server.useExternalCredentials ? true : false
		if (useExternalCredentials) {
		  def defaultFilePath = "${userHome}/credencials/${metadata.'app.name'}/tomcat.conf"
 		  println "$targetLabel server.externalCredentialsFile: ${server.externalCredentialsFile}"
 		  println "$targetLabel defaultFilePath: $defaultFilePath"
		  def configFile = (server.externalCredentialsFile == null || server.externalCredentialsFile.isEmpty()) ? defaultFilePath : server.externalCredentialsFile
		  println "$targetLabel Using credentials from file outside of repository: $configFile"		  
		  File file = new File(configFile)
      def tomcatCred = new ConfigSlurper().parse(file.toURL())
      println "$targetLabel Tomcat credentials: $tomcatCred"
      usr  = tomcatCred[scriptEnv][serverName].username
      pass = tomcatCred[scriptEnv][serverName].password      
		} else {
  		  println "$targetLabel Using MLDeploymentConfiguration credentials"
        usr  = server.username
        pass = server.password
		}
		println "$targetLabel Username: $usr. Password: $pass"
    
    def ts = [server: serverName, 
              username: usr,
              password: pass,  
              url: server.url,
              contextPath: server.contextPath,
              useExternalCredentials: server.useExternalCredentials,
              externalCredentialsFile: server.externalCredentialsFile]
    serversConfig.put(serverName, ts)
    println "$targetLabel Server parsed: $ts"
    println ""
    println ""    
	}

target(tomcatDeployApplication: 'Tomcat Deploy Application') {
  targetLabel = "[DEBUG - Tomcat Deploy App]"
    println ""
    println "               ***************"
    println "         Deploying Tomcat Application ..."
    println "               ***************"    
    println ""    
	depends(compile,createConfig,parseTomcatSettings)

  if (serversConfig?.size() > 0) {
      war()
    	serversConfig?.each { server ->
  	    println "$targetLabel Deploying server $server.key ($server.value)"
        println """
            $targetLabel war:$warName, 
                    url: $server.value.url,
                   path: $server.value.contextPath,
               username: $server.value.username,
               password: $server.value.password
             """  	   
        println "" 
        tomcatdeploy(war:warName,  
             url: server.value.url, 
             path: server.value.contextPath,
             username: server.value.username,
             password: server.value.password)
    } 
  } else {
  	  println "$targetLabel There is no server configured to deploy for the given environment ($scriptEnv)"
    }
}

/*
* @Out of date. Need Upgrade
*/
target(tomcatUndeployApplication: 'Undeploy Application') {
	depends(parseArguments,parseTomcatSettings)
  targetLabel = "[DEBUG - Tomcat Undeploy App]"
    println ""
    println "               ***************"
    println "         Undeploying Tomcat Application ..."
    println "               ***************"    
    println ""    
	depends(compile,createConfig,parseTomcatSettings)

	println '''\
NOTE: If you experience a classloading error during undeployment you need to take the following steps:					

* Upgrade to Tomcat 6.0.20 or above
* Pass this system argument to Tomcat: -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false

See http://tomcat.apache.org/tomcat-6.0-doc/config/systemprops.html for more information
'''
  if (serversConfig?.size() > 0) {
    	serversConfig?.each { server ->
  	    println "$targetLabel Undeploying server $server.key ($server.value)"
        println """
            $targetLabel url: $server.value.url,
                   path: $server.value.contextPath,
               username: $server.value.username,
               password: $server.value.password
             """  
        println ""             	    
        tomcatundeploy(url: server.value.url, 
             path: server.value.contextPath,
             username: server.value.username,
             password: server.value.password)
    } 
  } else {
  	  println "$targetLabel There is no server configured to undeploy for the given environment($scriptEnv)"
    }	   		
}

target(tomcatListApplications: 'List Applications') {
	depends(parseArguments,parseTomcatSettings)
  targetLabel = "[DEBUG - Tomcat List Apps]"
    println ""
    println "               ***************"
    println "         Listing Tomcat Applications ..."
    println "               ***************"    
    println ""    
	 if (serversConfig?.size() > 0) {
    	serversConfig?.each { server ->
        tomcatlist(url: server.value.url, 
             username: server.value.username,
             password: server.value.password)			    
			    
			 }
		} else {
  	  println "$targetLabel There is no server configured to list applications from for the given environment($scriptEnv)"
    }
}

/*
* Load complete tomcat servers configuration to Servers List
*/
target(parseTomcatSettings: 'Parses and sets up Tocat settings') {
    def targetLabel = "[DEBUG - Parse Tomcat Settings]"
    println "$targetLabel Init ..."

	  def params = argsMap.params
 		println "$targetLabel Params: $params"
	  targetServer = params ? params[0].trim() : null
	  argsMap.params.clear()
 		println "$targetLabel Target server: $targetServer"
    
    println "$targetLabel Opening configuration file: ${basedir}/grails-app/conf/mldeploy.groovy"
		def config = new ConfigSlurper().parse(new File("${basedir}/grails-app/conf/mldeploy.groovy").toURL())


	println "$targetLabel Start parsing servers for environment *** $scriptEnv *** ..."
	config[scriptEnv]?.servers?.each { server ->
	  println "$targetLabel Checking tomcat configuration for server: ${server.key} ..."
		if ((targetServer == null || targetServer.equals(server.key)) &&
		     !server.value.tomcat?.isEmpty()) {		
    		println "$targetLabel Parsing server ${server.key} ..."
		    parseTomcatServer(server.key, server.value.tomcat)
		} else {
				println "$targetLabel    No  'tomcat { ..}' section found!"
		}
	}
}


target(tomcatPrintHelp:"How to use this plugin") {
  println """
 *****************************      
*        Tomcat Mode          *
 *****************************
This mode uses the Grails Tomcat pluging
Available Targets:
- deploy
- undeploy <apps>
- list
  """
}

setDefaultTarget(tomcatDeployApplication)
