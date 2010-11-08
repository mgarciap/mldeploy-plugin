/**
*  Mercadolibre's New World Deployment Scripts: Scripted Version
*     - Uses secure-copy and secure-shell
*  2010-09-27 - Manuel: version inicial tomando lo de Leandro
*/

includeTargets << grailsScript("Init")

scriptEnv = Ant.project.properties.'grails.env'

//All environment Tomcat server Configurations
def serversConfig = [:]

// Single server name or ALL servers
def targetServers = []

parseSSHServer = {serverName, server ->
    def targetLabel = "[Parse SSH Server: $serverName - DEBUG]"
	  println "$targetLabel Parameters [serverName: $serverName, server: $server]"   
    def usr
    def pass
	  def defaultTargetWarDir = "/department/${metadata.'app.name'}/webapp"    
    def targetWarDir = (server.targetWarDir == null || server.targetWarDir.isEmpty()) ? defaultTargetWarDir : server.targetWarDir
		def useExternalCredentials = server.useExternalCredentials ? true : false
		def sshKeyFile
    println "$targetLabel IP Address/Hostname: ${server.host}"
	  println "$targetLabel Target application WAR directory: $defaultTargetWarDir"
    println "$targetLabel Authentication Mode: ${server.authenticationMode}"		
  switch(server.authenticationMode) {
    case 'credentialsFile': 
      def credFilePath
      if (server.externalCredentialsFile == null || server.externalCredentialsFile.isEmpty()) {
  		  credFilePath = "${userHome}/credencials/${metadata.'app.name'}/ssh.conf"
        println "$targetLabel No credentials file configured, using default: $credFilePath"
      } else {
  		  credFilePath = server.externalCredentialsFile
   		  println "$targetLabel Credentials file configured: $credFilePath"
      }
		  File file = new File(credFilePath)
 		  if (!file.exists() ) {
 		   println "$targetLabel    File dosn' exists. Script interrupted :("
 		   exit(1)
 		   }		  
      def sshCred = new ConfigSlurper().parse(file.toURL())
      println "$targetLabel SSH credentials: $sshCred"
      usr  = sshCred[scriptEnv][serverName].username
      pass = sshCred[scriptEnv][serverName].password      
      println "$targetLabel Server ${server.host} SSH authentication: $usr/$pass"		      
      break
      
    case 'sshKey': 
      if (server.sshKeyFile == null || server.sshKeyFile.isEmpty()) {
  		  sshKeyFile = "${userHome}/credencials/${metadata.'app.name'}/${metadata.'app.name'}-key"
        println "$targetLabel No credentials file configured, using default: $sshKeyFile"
      } else {
  		  sshKeyFile = server.sshKeyFile
   		  println "$targetLabel Credentials file configured: $sshKeyFile"
      }    
 		  File file = new File(sshKeyFile)
 		  if (!file.exists() ) {
 		   println "$targetLabel    File dosn' exists. Script interrupted :("
 		   exit(1)
 		   }
  	  usr  = server.username
      break
      
    default:  // basic
      usr  = server.username
      pass = server.password
      println "$targetLabel Server ${server.host} SSH authentication: $usr/$pass"		
    }  		
		
    def ts = [server: serverName, 
              authenticationMode: server.authenticationMode,
              host: server.host,
              warDir: targetWarDir,
              username: usr,
              password: pass,  
              sshKeyFile: sshKeyFile,
              //contextPath: server.contextPath, -> Lo debería manejar el script de deployment remoto
              useExternalCredentials: server.useExternalCredentials,
              externalCredentialsFile: server.externalCredentialsFile]
    serversConfig.put(serverName, ts) 
    println "$targetLabel Server parsed: $ts"
    println ""
    println ""    
	}

excecutePreDeployScripts = {server ->
  targetLabel = "[DEBUG - Excecute PreDeploy Scripts]"
  println "$targetLabel Not Implemented yet!"	
  println "$targetLabel Should use convention over configuration. Script in /deploy-scripts/ starting with 'pre-deploy*' pattern'!"	
  //ant.sshexec host:"${host}",username:"${username}",password:"${password}",command:"${command} ${deployAppVersion}"
}

excecutePostDeployScripts = {server ->
  targetLabel = "[DEBUG - Excecute PostDeploy Scripts]"
  println "$targetLabel Not Implemented yet!"	
  println "$targetLabel Should use convention over configuration. Script in /deploy-scripts/ starting with post-deploy*!"	
}

target(scriptedDeployApplication: 'Deply Webapp using scripts') {
	  depends(compile,parseServerSettings)
    targetLabel = '[DEBUG - Scripted Deploy]'
    sshTimeOut = 30 * 60 * 1000 // 30 minutes
    println ""
    println "               ***************"
    println "         Deploying Tomcat Application"
    println "                (Using scripts) ..."
    println "               ***************"    
    println ""    
    println "$targetLabel	Custom Environment: $scriptEnv"
    println ""
    println ""            
    
    if (serversConfig?.size() > 0) {
      war()
    	serversConfig?.each { server ->
    	  excecutePreDeployScripts(server.key)
        servData = server.value
        appVersion = metadata.'app.version'
        appName = metadata.'app.name'
  	    targetDir = "${servData.warDir}/$appVersion"
        println "$targetLabel Starting Secure Copy of war file: $warName to server: ${server.key}(${servData.host})"
        println "$targetLabel    Authenticadion Mode: ${server.value.authenticationMode}"
        
        //remote commands:
        createDirCmd = "rm -rf $targetDir ; mkdir $targetDir"
        deployCmd = "/department/$appName/bin/apply-version $appName $appVersion"
  	    if (servData.authenticationMode.equals("sshKey")) {
  	      //TODO: Implement Paraphare use. Now empty mode. Take it from config file?
    	    passphrase = ""

          ant.sshexec host:servData.host,username:servData.username,keyfile:servData.sshKeyFile, timeout:sshTimeOut, passphrase:passphrase, verbose:true, trust:true, command:createDirCmd
    	    
      		println "$targetLabel ant.scp file:$warName, todir:${servData.username}@${servData.host}:$targetDir/., keyfile:${servData.sshKeyFile},passphrase:$passphrase,verbose:true, trust:true"
      		
    			ant.scp file:"$warName", todir:"${servData.username}@${servData.host}:${targetDir}/.", keyfile:servData.sshKeyFile, passphrase:passphrase, verbose:true, trust:true
          println "$targetLabel    Secure Copy finished"
    	    println "$targetLabel Deploying server ${server.key}"
          ant.sshexec host:servData.host,username:servData.username,keyfile:servData.sshKeyFile, timeout:sshTimeOut, passphrase:passphrase, verbose:true, trust:true, command:deployCmd
  	    } else {
  	      // let's create version directory
          ant.sshexec host:servData.host, username:servData.username, password:servData.password, timeout:sshTimeOut, verbose:true, trust:true, command:createDirCmd
  	      println "$targetLabel ant.scp file:$warName, todir:${servData.username}@${servData.host}:$targetDir/., password:${servData.password},verbose:true, trust:true"  	      
     			ant.scp file:"$warName", todir:"${servData.username}@${servData.host}:${targetDir}/.", password:servData.password, verbose:true, trust:true
          println "$targetLabel Secure Copy finished"
        
    	    println "$targetLabel Deploying server $server.key (restart Tomcat Instance)"
          ant.sshexec host:servData.host, username:servData.username, password:servData.password, command:deployCmd    
  	    }  	    
     	  excecutePostDeployScripts(server.key)
  	  }
  	} else {
  	  println "$targetLabel There is no server configured to deploy for the given environment"
  	}
  }	

target(scriptedUndeployApplication: 'scriptedUndeployApplication') {
  println "scriptedUndeployApplication: Not implemented yet!!!"
  
}

target(scriptedRollbackApplication: 'scriptedRollbackApplication') {
  println "scriptedRollbackApplication: Not implemented yet!!!"
}

target(scriptedPredeployScript: 'scriptedPredeployScript') {
  println "scriptedPredeployScript: Not implemented yet!!!"
}

target(scriptedPostdeployScript: 'scriptedPostdeployScript') {
  println "scriptedPostdeployScript: Not implemented yet!!!"
}



/*
* Load complete tomcat servers configuration to Servers List
*/
target(parseServerSettings: 'Parses and sets up Tocat server settings') {
    def targetLabel = "[DEBUG - Parse Tomcat Settings]"
    println "$targetLabel Init ..."

	  def params = argsMap.params
 		println "$targetLabel Params: $params"
	  targetServer = params ? params[0].trim() : null
	  argsMap.params.clear()
 		println "$targetLabel Target server: $targetServers"
    
    println "$targetLabel Opening configuration file: ${basedir}/grails-app/conf/mldeploy.groovy"
		def config = new ConfigSlurper().parse(new File("${basedir}/grails-app/conf/mldeploy.groovy").toURL())


	println "$targetLabel Start parsing servers for environment *** $scriptEnv *** ..."
	config[scriptEnv]?.servers?.each { server ->
		if ((targetServer == null || targetServer.equals(server.key)) &&
		     !server.value.scripted?.isEmpty()) {		
  		parseSSHServer(server.key, server.value.scripted)
  	} else {
				println "$targetLabel    No  'scripted { ..}' section found!"
		}
	}
	println "$targetLabel Servers for environment '$scriptEnv' parsed: ${serversConfig.size()}"
	println "$targetLabel Configured Target Servers: $serversConfig"
  println "$targetLabel Finished!"
}

target(scriptedPrintHelp:"How to use this plugin mode") {
  println """
 *****************************      
* New World Deployment Plugin *
*       Scripted Mode         *
 *****************************
This mode uses customs groovy Scripts
Available commands:
- deploy [webServerName]
- undeploy [webServerName]
- list [webServerName]

-webServerName
     Optional. Every single configured server if not specified.
  """
}

setDefaultTarget(scriptedDeployApplication)

